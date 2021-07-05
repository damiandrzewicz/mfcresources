package de.atos.solumversion.services;

import org.junit.jupiter.api.*;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SubversionServiceIT {

    static SubversionService subversionService = new SubversionService();

    static final String url = "http://localhost/svn/SolumSTAR";
    static final String user = "test";
    static final String password = "test123";
    static final String workingFolder = "svn";
    static File workingDir;

    @BeforeAll
    static void beforeAll(){
        String path = "src/test/resources";
        String workingPath = path + "/" + workingFolder;
        workingDir = new File(workingPath);
        String abs = workingDir.getAbsolutePath();
        workingDir.mkdirs();
        workingDir.deleteOnExit();
    }

    @AfterAll
    static void afterAll(){
        FileSystemUtils.deleteRecursively(workingDir);
    }


    @Test
    @Order(1)
    void open_noAuth() throws SVNException {
        assertThrows(SVNAuthenticationException.class, () -> subversionService.open(SVNURL.parseURIEncoded(url)));
    }

    @Test
    @Order(2)
    void open_badUrl(){
        assertThrows(SVNException.class, () -> subversionService.open(SVNURL.parseURIEncoded("")));
    }

    @Test
    @Order(3)
    void authenticate() {
        assertDoesNotThrow(() -> subversionService.authenticate(user, password));
    }

    @Test
    @Order(4)
    void open_goodAuth(){
        subversionService.authenticate(user, password);
        assertDoesNotThrow(() -> subversionService.open(SVNURL.parseURIEncoded(url)));
    }

    @Test
    @Order(5)
    void getRootDirEntires(){
        assertDoesNotThrow(() -> {
            Collection dirEntries = subversionService.getDirEntries("", SVNRevision.HEAD);
            assertFalse(dirEntries.isEmpty());
        });
    }

    @Test
    @Order(6)
    void checkoutEmptyFolder(){
        File file = new File(workingDir.getPath() + "/SolumSTAR/");
        assertDoesNotThrow(() -> {
            long l = subversionService.checkoutFolder(SVNURL.parseURIEncoded(url), SVNRevision.HEAD, file, SVNDepth.EMPTY);
            System.out.println(l);
        });
    }

    @Test
    @Order(7)
    void updateFile(){
        String relativePath = "/Src/ACRadio/ACRAD.RC";
        File file = new File(workingDir.getPath() + "/SolumSTAR/" + relativePath);
        assertDoesNotThrow(() -> {
            subversionService.updateFiles(new File[]{file}, SVNRevision.HEAD, SVNDepth.FILES);
        });
    }

    @Test
    @Order(8)
    void listModifiedfiles(){
        File file = new File(workingDir.getPath() + "/SolumSTAR/");
        assertDoesNotThrow(() -> {
            List<File> files = subversionService.listModifiedFiles(file, SVNRevision.HEAD);
            System.out.println(files);
        });
    }

    @Test
    @Order(9)
    void wcInfo(){
        File file = new File(workingDir.getPath() + "/SolumSTAR/");
        assertDoesNotThrow(() -> {
            SVNInfo svnInfo = subversionService.wcInfo(file, SVNRevision.HEAD);
            System.out.println("asd");
        });
    }



    //@Test
//    @Order(10)
//    void commit(){
//        String relativePath = "/Src/ACRadio/ACRAD.RC";
//        File file = new File(workingDir.getPath() + "/SolumSTAR/" + relativePath);
//        assertDoesNotThrow(() -> {
//            SVNCommitInfo info = subversionService.commit(file, false, "test message");
//            System.out.println(info.getNewRevision());
//        });
//    }
    



}