package gitlet;

import java.io.File;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Cansin Rodoplu
 */
public class Repository {


    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The commits subdirectory. */
    public static final File COMMITS = join(GITLET_DIR, "commits");
    /** Blobs subdirectory. */
    public static final File BLOBS = join(GITLET_DIR, "blobs");
    /** StagingArea subdirectory. */
    public static final File STAGINGAREA = join(GITLET_DIR, "StagingArea");
    /** Heads SubDirectory. */
    public static final File HEADS = join(GITLET_DIR, "HEADS");


    /**  Initialize GITLET DIR */

    public static void initHelper(String[] args) {
        if (GITLET_DIR.exists()) {
            System.out.println(
                    "A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        COMMITS.mkdir();
        BLOBS.mkdir();

        Staged stageObj = new Staged();
        Utils.writeObject(STAGINGAREA, stageObj);

        Commit initial = new Commit();
        Commit.saveCommit(initial);
        Heads heads = new Heads();
        heads.mapCommitMaster(initial.getId(), initial);
        heads.mapActiveHead(initial);
        heads.mapAllHeads(initial.getId(), initial);
        heads.saveHead(heads);
    }
}
