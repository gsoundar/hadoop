/*
 * Copyright 2015 Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.fs;

import java.io.File;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.crypto.key.JavaKeyStoreProvider;
import org.apache.hadoop.fs.nfs.NFSv3FileSystem;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.hdfs.nfs.conf.NfsConfiguration;
import org.apache.hadoop.hdfs.nfs.nfs3.Nfs3;
import org.apache.hadoop.security.authorize.DefaultImpersonationProvider;
import org.apache.hadoop.security.authorize.ProxyUsers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FooTestNfsFileSystemContractBaseTest extends
        FileSystemContractBaseTest {

    MiniDFSCluster cluster = null;
    Nfs3 nfs3 = null;

    public static final int NFSD_PORT = 50000;
    public static final int MOUNTD_PORT = 50001;
    
    public static final Log LOG = LogFactory
            .getLog(FooTestNfsFileSystemContractBaseTest.class);

    @Before
    @Override
    protected void setUp() throws Exception {

        MiniDFSCluster _cluster = null;
        Nfs3 _nfs3 = null;
        boolean errored = false;
        
        try {
            // Set up impersonations
            String currentUser = System.getProperty("user.name");
            NfsConfiguration config = new NfsConfiguration();
            config.set("fs.permissions.umask-mode", "u=rwx,g=,o=");
            config.set(DefaultImpersonationProvider.getTestProvider()
                    .getProxySuperuserGroupConfKey(currentUser), "*");
            config.set(DefaultImpersonationProvider.getTestProvider()
                    .getProxySuperuserIpConfKey(currentUser), "*");
            FileSystemTestHelper fsHelper = new FileSystemTestHelper();

            // Set up java key store
            String testRoot = fsHelper.getTestRootDir();
            File testRootDir = new File(testRoot).getAbsoluteFile();
            final Path jksPath = new Path(testRootDir.toString(), "test.jks");
            config.set(DFSConfigKeys.DFS_ENCRYPTION_KEY_PROVIDER_URI,
                    JavaKeyStoreProvider.SCHEME_NAME + "://file" + jksPath.toUri());
            ProxyUsers.refreshSuperUserGroupsConfiguration(config);

            // Set NFS/MOUNT port
            int _port = NFSD_PORT;
            for(int i = 0; i < 5; i+=2) {
                try {
                    _port = NFSD_PORT + i;
                    config.setInt("nfs3.server.port", 0);
                    config.setInt("nfs3.mountd.port", 0);
                    
                    // Start HDFS minicluster
                    _cluster = new MiniDFSCluster.Builder(config).numDataNodes(1).build();
                    _cluster.waitActive();

                    // Start nfs
                    _nfs3 = new Nfs3(config);
                    _nfs3.startServiceInternal(true);
                    break;
                } catch(Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
            
            Configuration conf = new Configuration();
            conf.setBoolean("mambo.test", true);
            conf.setInt("fs.nfs.mount.port", _port + 1);
            conf.set("fs.nfs.auth.flavor", "AUTH_SYS");

            fs = new NFSv3FileSystem();
            fs.initialize(new URI("nfs://localhost:" + _port), conf);
            assertNotNull(fs);

        } catch (Exception exception) {
            LOG.info("Got an exception while trying to setup test", exception);
            if(_nfs3 != null) {
                _nfs3 = null;
            }
            if(_cluster != null) {
                _cluster.shutdown();
                _cluster = null;
            }
            errored = true;
        } finally {
            if(!errored) {
                cluster = _cluster;
                nfs3 = _nfs3;
            }
        }

    }

    @After
    public void teardown() throws Exception {
        if (cluster != null) {
            cluster.shutdown();
            cluster = null;
            nfs3 = null;
        }
        if (fs != null) {
            fs.close();
            fs = null;
        }
    }

    @Test
    public void testAAAA() throws Exception {
        assertTrue(false);
    }

}
