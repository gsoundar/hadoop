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


import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.nfs.NFSv3FileSystem;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.hdfs.nfs.conf.NfsConfiguration;
import org.apache.hadoop.hdfs.nfs.nfs3.Nfs3;
import org.apache.hadoop.hdfs.nfs.nfs3.RpcProgramNfs3;
import org.junit.Test;


public class TestNfsFileSystemContractBaseTest {
    
	public static final Log LOG = LogFactory.getLog(TestNfsFileSystemContractBaseTest.class);
    
    @Test
    public void testBlah() throws Exception {
        
    	// Start minicluster
        NfsConfiguration config = new NfsConfiguration();
        MiniDFSCluster cluster = new MiniDFSCluster.Builder(config).numDataNodes(1).build();
        cluster.waitActive();
        
        // Use ephemeral port in case tests are running in parallel
        config.setInt("nfs3.mountd.port", 50000);
        config.setInt("nfs3.server.port", 50001);
        
        // Start nfs
        Nfs3 nfs3 = new Nfs3(config);
        nfs3.startServiceInternal(false);
        RpcProgramNfs3 nfsd = (RpcProgramNfs3) nfs3.getRpcProgram();
    	LOG.info("NFS server running on port=" + nfsd.getPort());
    	
        Configuration conf = new Configuration();
        conf.setBoolean("mambo.test", true);
        FileSystem fs = new NFSv3FileSystem();
        fs.initialize(new URI("nfs://localhost:" + 50001), conf);
        //fs.delete(path("/test"), true);
    	
    }
    
}
