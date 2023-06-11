package gitlet;
import java.util.HashMap;
import java.io.Serializable;
import java.util.Map;

/** Head Object contains HashMaps.
 * For easy access to Commits.
 * @author  Cansin Rodoplu */
public class Heads implements Serializable {

    /** Private Instances*/

    /** From Commit ID to Current Commit.
     * 1 pair of key-value at all times.*/
    HashMap<String, Commit> toMaster;

    /** Branch Name to Last Commit.
     * HashMap contains n key-value pairs.
     * n is the number of branches in repo.*/
    HashMap<String, Commit> toActiveHead;

    /** HashMap mapping from each ID of Commits to the Commit.
     * This Map contains maps from ALL the Commit IDs and the existing Commits. */
    HashMap<String, Commit> toAllHeads;

    /** Active Branch Name,
     *  referring to which Branch we add new commits*/
    String activeBranch;

    String errorMessage = "There is an untracked file in the way; "
            + "delete it, or add and commit it first.";

    public Heads() {
        toActiveHead = new HashMap<>();
        toMaster = new HashMap<>();
        toAllHeads = new HashMap<>();
        activeBranch = "main";

    }
    /**
     * Creates a new Branch.
     * the new Branch points the last commit.*/
    public static void branchHelper(String[] args) {

        Heads headsObj = Utils.readObject(Repository.HEADS, Heads.class);

        if (args.length != 2) {
            System.out.println("Incorrect Operands.");
            System.exit(0);
        }

        if (headsObj.toActiveHead.containsKey(args[1])) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        for (Map.Entry<String, Commit> toActiveHeadSet: headsObj.toActiveHead.entrySet()) {
            if (toActiveHeadSet.getKey().equals(headsObj.activeBranch)) {
                headsObj.toActiveHead.put(args[1], toActiveHeadSet.getValue());
            }
        }
        headsObj.saveHead(headsObj);
    }

    /** Description: Deletes the branch with the given name.
     * This only means to delete the pointer associated with the branch;
     * it does not mean to delete all commits that were created under the branch,
     * or anything like that.
     * If a branch with the given name does not exist, aborts.
     * Print the error message A branch with that name does not exist.
     * If you try to remove the branch you’re currently on, aborts, printing the error message
     * Cannot remove the current branch.
     * */

    public static void branchRemoveHelper(String[] args) {

        Heads headsObj = Utils.readObject(Repository.HEADS, Heads.class);

        if (args[1].equals(headsObj.activeBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        if (!(headsObj.toActiveHead.containsKey(args[1]))) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        headsObj.toActiveHead.remove(args[1]);

        headsObj.saveHead(headsObj);

    }

    /** Maps CommitID to RecentCommit in Heads Object*/
    public void mapCommitMaster(String commitID, Commit recentCommit) {
        toMaster.put(commitID, recentCommit);
    }

    /** Maps Active Branch to the last commit.*/
    public void mapActiveHead(Commit activeCommit) {
        toActiveHead.put(activeBranch, activeCommit);
    }


    /** Removes the Branch Name and
     * last commit from the HashMap */

    /** Maps CommitID to the Commit in
     * Heads Object in order to store all the paths to Commit*/
    public void mapAllHeads(String commitID, Commit commit) {
        toAllHeads.put(commitID, commit);
    }

    /** Saves Heads Object in  HEADS Directory*/
    public void saveHead(Heads heads) {
        Utils.writeObject(Repository.HEADS, heads);
    }
}
