/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.seam.cron;

import java.io.File;
import java.io.Serializable;
import org.jboss.arquillian.api.Deployment;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 *
 * @author Peter Royle
 */
public abstract class SeamCronTest implements Serializable {

    private static Logger log = Logger.getLogger(CronTest.class);
        
    @Deployment
    public static JavaArchive createDefaultArchive() 
    {
    	JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
    		.addPackages(true,"org.jboss.seam.cron")
    		.addAsManifestResource(
    			new File("src/main/resources/META-INF/beans.xml"), 
    			ArchivePaths.create("beans.xml"))
    		.addAsManifestResource(
			new File("src/main/resources/META-INF/services/javax.enterprise.inject.spi.Extension"), 
			ArchivePaths.create("services/javax.enterprise.inject.spi.Extension"));
    	
        log.debug(archive.toString(true));
    	return archive;
    }


    public SeamCronTest() {
    }
    
}
