package gitlet;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Cansin Rodoplu
 */
public class Main {

    /** Main Class for GITLET
     */

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                Repository.initHelper(args);
                break;
            case "add":
                Commit.addHelper(args);
                break;
            case "commit":
                Commit.commitHelper(args);
                break;
            case "rm":
                Commit.removeHelper(args);
                break;
            case "checkout":
                if (args.length == 3) {
                    Commit.checkoutLastCommit(args);
                }
                if (args.length == 4) {
                    Commit.checkoutGivenID(args);
                }
                if (args.length == 2) {
                    Commit.checkoutBranch(args);
                }
                break;
            case "log":
                Commit.logHelper(args);
                break;
            case "branch":
                Heads.branchHelper(args);
                break;
            case "global-log":
                Commit.globalLogHelper();
                break;
            case "find":
                Commit.findHelper(args);
                break;
            case "status":
                Commit.statusHelper();
                break;
            case "rm-branch":
                Heads.branchRemoveHelper(args);
                break;
            case "reset":
                Commit.resetHelper(args);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
}
