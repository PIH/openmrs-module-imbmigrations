package org.openmrs.module.imbmigrations.migrations;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;

import java.util.List;

/**
 * If you unignore and run this unit test, it will attempt to remove the duplicate CIEL concept source
 */
@Ignore
public class DeleteDuplicateConceptSourceTest extends StandaloneContextSensitiveTest {

    @Autowired
    ConceptService conceptService;

    @Test
    @NotTransactional
    public void testReasonForNotDoingFollowupCohort() throws Exception {

        // Verify that the term and source we wish to keep are in place

        ConceptSource sourceToKeep = conceptService.getConceptSource(9);
        Assert.assertEquals("CIEL", sourceToKeep.getName());

        Integer numTermsToKeep = conceptService.getCountOfConceptReferenceTerms(null, sourceToKeep, true);
        Assert.assertTrue(numTermsToKeep.intValue() > 1);

        ConceptReferenceTerm termToKeep = conceptService.getConceptReferenceTermByCode("84668", sourceToKeep);
        Assert.assertNotNull(termToKeep);

        // Now, verify we can get rid of the other one and do so

        ConceptSource sourceToDelete = conceptService.getConceptSource(18);
        Assert.assertEquals("CIEL", sourceToDelete.getName());

        Integer numTermsToDelete = conceptService.getCountOfConceptReferenceTerms(null, sourceToDelete, true);
        Assert.assertEquals(1, numTermsToDelete.intValue());

        ConceptReferenceTerm termToDelete = conceptService.getConceptReferenceTermByCode("84668", sourceToDelete);
        Assert.assertNotNull(termToDelete);

        List<Concept> mappedConcepts = conceptService.getConceptsByMapping("84668", "CIEL", true);
        Assert.assertEquals(1, mappedConcepts.size());

        Concept concept = mappedConcepts.get(0);
        ConceptMap mapToRemove = null;
        for (ConceptMap m : concept.getConceptMappings()) {
            if (m.getConceptReferenceTerm().getConceptSource().equals(sourceToDelete)) {
                mapToRemove = m;
            }
        }
        concept.removeConceptMapping(mapToRemove);

        conceptService.saveConcept(concept);
        conceptService.purgeConceptReferenceTerm(termToDelete);
        conceptService.purgeConceptSource(sourceToDelete);
    }

}
