package org.openmrs.module.imbmigrations.migrations;

import org.junit.Before;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.springframework.test.context.ContextConfiguration;

import java.util.Properties;

@SkipBaseSetup
public abstract class StandaloneContextSensitiveTest extends BaseModuleContextSensitiveTest {

	@Override
	public Boolean useInMemoryDatabase() {
		return false;
	}

	/**
	 * @return MS Note: use port 3306 as standard, 5538 for sandbox 5.5 mysql environment
	 */
	@Override
	public Properties getRuntimeProperties() {
		Properties p = super.getRuntimeProperties();
        p.setProperty("connection.url", "jdbc:mysql://localhost:3308/openmrs?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8");
        p.setProperty("connection.username", "root");
        p.setProperty("connection.password", "root");
        p.setProperty("junit.username", "admin");
        p.setProperty("junit.password", "Admin123");
		return p;
	}

    @Before
	public void setupForTest() throws Exception {
        if (!Context.isSessionOpen()) {
            Context.openSession();
        }
        Context.clearSession();
        authenticate();
    }

	@Override
	public void deleteAllData() throws Exception {
	}
}
