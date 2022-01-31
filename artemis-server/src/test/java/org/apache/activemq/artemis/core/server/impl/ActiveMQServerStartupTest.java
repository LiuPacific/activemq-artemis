/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.activemq.artemis.core.server.impl;

import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.logs.AssertionLoggerHandler;
import org.apache.activemq.artemis.tests.util.ActiveMQTestBase;
import org.apache.activemq.artemis.utils.critical.CriticalAnalyzerAccessor;
import org.apache.activemq.artemis.utils.critical.CriticalAnalyzerPolicy;
import org.apache.activemq.artemis.utils.critical.CriticalComponentImpl;
import org.junit.Assert;
import org.junit.Test;

public class ActiveMQServerStartupTest extends ActiveMQTestBase {

   @Test
   public void testTooLongToStartHalt() throws Exception {
      testTooLongToStart(CriticalAnalyzerPolicy.HALT);
   }

   @Test
   public void testTooLongToStartShutdown() throws Exception {
      testTooLongToStart(CriticalAnalyzerPolicy.SHUTDOWN);
   }

   @Test
   public void testTooLongToStartLOG() throws Exception {
      testTooLongToStart(CriticalAnalyzerPolicy.LOG);
   }

   private void testTooLongToStart(CriticalAnalyzerPolicy policy) throws Exception {

      AssertionLoggerHandler.startCapture();
      try {

         ConfigurationImpl configuration = new ConfigurationImpl();
         configuration.setCriticalAnalyzerPolicy(policy);
         configuration.setCriticalAnalyzer(true);
         configuration.setPersistenceEnabled(false);
         ActiveMQServerImpl server = new ActiveMQServerImpl(configuration);
         addServer(server);
         server.start();
         // this will be faking a condition
         server.setState(ActiveMQServer.SERVER_STATE.STARTING);
         CriticalAnalyzerAccessor.fireActions(server.getCriticalAnalyzer(), new CriticalComponentImpl(server.getCriticalAnalyzer(), 2));
         Assert.assertTrue(AssertionLoggerHandler.findText("224116"));
         Assert.assertEquals(ActiveMQServer.SERVER_STATE.STARTING, server.getState()); // should not be changed
         server.stop();
      } finally {
         AssertionLoggerHandler.stopCapture();
      }
   }

}
