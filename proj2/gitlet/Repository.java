package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet/");
    //暂存区
    public static final File STAGE_DIR = Utils.join(GITLET_DIR,"stage/");
    //blobs
    public static final File BLOB_DIR = Utils.join(GITLET_DIR,"blobs/");
    //commits
    public static final File COMMIT_DIR = Utils.join(GITLET_DIR,"commits/");

    /* TODO: fill in the rest of this class. */
    public static boolean exsistGit(){
        return GITLET_DIR.exists();
    }
    public static void setupPersistence(){
        //不存在.git
        if(!exsistGit()){
            GITLET_DIR.mkdir();
        }
        if(!STAGE_DIR.exists()){
            STAGE_DIR.mkdir();
        }
        if(!BLOB_DIR.exists()){
            BLOB_DIR.mkdir();
        }
        if(!COMMIT_DIR.exists()){
            COMMIT_DIR.mkdir();
        }
    }

}
