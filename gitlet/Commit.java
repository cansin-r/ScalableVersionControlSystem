package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


import java.text.SimpleDateFormat;

import static gitlet.Utils.*;


/** Represents a gitlet commit object.
 *  The commit object takes in a message.
 *  It has a timestamp and a Commit parent.
 *  The commit object has a reference to blobs
 *  of files through the blob's hash id.
 *  Commit objects are stored in .gitlet file.
 *  hash function to name Commit objects.
 *  @author Cansin Rodoplu
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */


    /**
     * The message of this Commit.
     */
    private String message;
    /**
     * Allocates a Date object and initializes it to represent the specified number of milliseconds
     * since the standard base time known as "the epoch", namely January 1, 1970, 00:00:00 GMT.
     */
    private Date timestamp;
    /**
     * String indicating the Parent ID of the Commit.
     */
    private String parent;
    /**
     * the special commit id.
     */
    private String id;
    /**
     * HashMap mapping from the file name to Blob Object.
     */
    private HashMap<String, Blobs> toBlob;

    /**
     * Initialized DateFormat.
     */
    public static final SimpleDateFormat DATEFORMAT =
            new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z",
            new Locale("en", "US"));


    /**
     * Constructor for Commit Object.
     * When Commit is Initial Commit.
     */
    public Commit() {
        this.message = "initial commit";
        this.timestamp = new Date(0);
        this.parent = null;
        this.id = Utils.sha1("initial commit");
    }

    /**
     * Constructor for Commit Object.
     * @param message
     */
    public Commit(String message) {
        this.message = message;
        this.timestamp = new Date();
        toBlob = new HashMap<>();

    }

    /**
     * Helper Method for Add.
     */

    public static void addHelper(String[] args) {
        Staged tempObj = Staged.readStagingArea();
        Heads headsObj = Utils.readObject(Repository.HEADS, Heads.class);

        if (!(Repository.GITLET_DIR.exists())) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }

        File f = Utils.join(Repository.CWD, args[1]);
        if (!(f.exists())) {
            System.out.println("File does not exist.");
            System.exit(0);
        }


        Boolean isInCurrentCommit = false;

        for (Map.Entry<String, Commit> toCurrentCommit: headsObj.toMaster.entrySet()) {
            if (!(toCurrentCommit.getValue().message.equals("initial commit"))) {
                for (Map.Entry<String, Blobs> toBlob
                        : toCurrentCommit.getValue().toBlob.entrySet()) {
                    if ((toBlob.getKey().equals(args[1]))) {
                        if (Arrays.equals(toBlob.getValue().getContents(), Utils.readContents(f))) {
                            isInCurrentCommit = true;
                        }
                    }
                }
            }
        }


        if (tempObj.removedFiles.containsKey(args[1])) {
            tempObj.removedFiles.remove(args[1]);
        } else if (isInCurrentCommit) {
            System.exit(0);
        } else {
            tempObj.mapAddedFiles(args[1], Utils.readContents(f));
        }


        Utils.writeObject(Repository.STAGINGAREA, tempObj);

    }

    /**
     * Commit Helper Method.
     * @param args
     */

    public static void commitHelper(String[] args) {
        Staged stageObj = Staged.readStagingArea();
        Heads headsObj = Utils.readObject(Repository.HEADS, Heads.class);
        if (!(Repository.GITLET_DIR.exists())) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (!(stageObj.moreToAdd) && !(stageObj.moreToRemove)) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if ((args.length != 2) || args[1].isBlank()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit newCommit = new Commit(args[1]);
        if (stageObj.moreToRemove && !stageObj.moreToAdd) {
            for (Map.Entry<String, Commit> toMasterSet
                    : headsObj.toMaster.entrySet()) {
                for (Map.Entry<String, Blobs> toParentBlobSet
                       : toMasterSet.getValue().toBlob.entrySet()) {
                    if (!(stageObj.removedFiles.containsKey(toParentBlobSet.getKey()))) {
                        newCommit.mapBlob(toParentBlobSet.getKey(), toParentBlobSet.getValue());
                    }
                }
            }
            stageObj.moreToRemove = false;
            stageObj.removedFiles.clear();
            stageObj.saveStage(stageObj);
        } else if (!stageObj.moreToRemove && stageObj.moreToAdd) {
            for (Map.Entry<String, Commit> toMasterSet
                    : headsObj.toMaster.entrySet()) {
                if (!(toMasterSet.getValue().message.equals("initial commit"))) {
                    for (Map.Entry<String, Blobs> toParentBlobSet
                          : toMasterSet.getValue().toBlob.entrySet()) {
                        newCommit.mapBlob(toParentBlobSet.getKey(), toParentBlobSet.getValue());
                    }
                }
            }
            for (Map.Entry<String, byte[]> addedFilesSet
                    :stageObj.addedFiles.entrySet()) {
                Blobs newBlob = new Blobs(addedFilesSet.getKey(), addedFilesSet.getValue());
                newCommit.mapBlob(addedFilesSet.getKey(), newBlob);
            }
            stageObj.addedFiles.clear();
            stageObj.moreToAdd = false;
            stageObj.saveStage(stageObj);
        } else if (stageObj.moreToAdd && stageObj.moreToRemove) {
            for (Map.Entry<String, Commit> toMasterSet : headsObj.toMaster.entrySet()) {
                for (Map.Entry<String, Blobs> toParentBlobSet
                        : toMasterSet.getValue().toBlob.entrySet()) {
                    if (!(stageObj.removedFiles.containsKey(toParentBlobSet.getKey()))) {
                        newCommit.mapBlob(toParentBlobSet.getKey(), toParentBlobSet.getValue());
                    }
                }
            }
            for (Map.Entry<String, byte[]> addedFilesSet: stageObj.addedFiles.entrySet()) {
                Blobs newBlob = new Blobs(addedFilesSet.getKey(), addedFilesSet.getValue());
                newCommit.mapBlob(addedFilesSet.getKey(), newBlob);
            }
            stageObj.moreToRemove = false;
            stageObj.moreToAdd = false;
            stageObj.addedFiles.clear();
            stageObj.removedFiles.clear();
        }
        for (Map.Entry<String, Commit> toActiveHead  : headsObj.toActiveHead.entrySet()) {
            newCommit.parent = toActiveHead.getValue().id;
        }
        StringBuilder enhancedID = new StringBuilder();
        enhancedID.append(newCommit.message);
        enhancedID.append(newCommit.parent);
        enhancedID.append(newCommit.timestamp.toString());
        newCommit.id = Utils.sha1(enhancedID.toString());
        headsObj.mapAllHeads(newCommit.getId(), newCommit);
        headsObj.toMaster.clear();
        headsObj.mapCommitMaster(newCommit.getId(), newCommit);
        headsObj.mapActiveHead(newCommit);
        headsObj.saveHead(headsObj);
        saveCommit(newCommit);
    }
    /** Status Helper.*/

    public static void statusHelper() {
        if (!(Repository.GITLET_DIR.exists())) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        Heads headsObj = Utils.readObject(Repository.HEADS, Heads.class);
        Staged stagedObj = Utils.readObject(Repository.STAGINGAREA, Staged.class);

        ArrayList<String> sortedBranchNames = new ArrayList<>(headsObj.toActiveHead.keySet());
        Collections.sort(sortedBranchNames);


        System.out.println("=== Branches ===");
        for (String x: sortedBranchNames) {
            if (x.equals(headsObj.activeBranch)) {
                System.out.println("*" + x);
            } else {
                System.out.println(x);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        if (!(stagedObj.addedFiles.isEmpty())) {
            ArrayList<String> sortedStagedFiles = new ArrayList<>(stagedObj.addedFiles.keySet());
            Collections.sort(sortedStagedFiles);
            for (String s: sortedStagedFiles) {
                System.out.println(s);
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        if (!(stagedObj.removedFiles.isEmpty())) {
            ArrayList<String> sortedRemovedFiles = new ArrayList<>(stagedObj.removedFiles.keySet());
            Collections.sort(sortedRemovedFiles);
            for (String r: sortedRemovedFiles) {
                System.out.println(r);
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /**
     * Checkout the last commit.
     * @param args
     */
    public static void checkoutLastCommit(String[] args) {

        Heads tempHeads = readObject(Repository.HEADS, Heads.class);
        Boolean fileExists = false;

        for (Map.Entry<String, Commit> toActiveHeadSet : tempHeads.toActiveHead.entrySet()) {
            if (toActiveHeadSet.getKey().equals(tempHeads.activeBranch)) {
                HashMap<String, Blobs> tempBlobHashMap = toActiveHeadSet.getValue().toBlob;
                for (Map.Entry<String, Blobs> toBlobSet : tempBlobHashMap.entrySet()) {
                    String fileName = toBlobSet.getKey();
                    if (fileName.equals(args[2])) {
                        fileExists = true;
                        byte[] contents = toBlobSet.getValue().getContents();
                        File f = Utils.join(Repository.CWD, args[2]);
                        if (f.exists()) {
                            Utils.writeContents(f, contents);
                            break;
                        } else {
                            File newFile = new File(Repository.CWD, args[2]);
                            Utils.writeContents(newFile, contents);
                            break;
                        }
                    }
                }
            }
        }

        if (!fileExists) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    /**
     * Checkout Given ID.
     * @param args
     */

    public static void checkoutGivenID(String[] args) {
        Heads headsObj = readObject(Repository.HEADS, Heads.class);

        if (!(args[2].equals("--"))) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }

        Boolean commitExists = false;
        Boolean fileExists = false;

        for (Map.Entry<String, Commit> toAllHeads : headsObj.toAllHeads.entrySet()) {
            if (toAllHeads.getKey().equals(args[1])) {
                commitExists = true;
                HashMap<String, Blobs> tempBlobsHashMap = toAllHeads.getValue().toBlob;
                for (Map.Entry<String, Blobs> toBlobSet : tempBlobsHashMap.entrySet()) {
                    String fileName = toBlobSet.getKey();
                    if (fileName.equals(args[3])) {
                        fileExists = true;
                        byte[] contents = toBlobSet.getValue().getContents();
                        File f = Utils.join(Repository.CWD, args[3]);
                        if (f.exists()) {
                            Utils.writeContents(f, contents);
                            break;
                        } else {
                            File newFile = new File(Repository.CWD, args[3]);
                            Utils.writeContents(newFile, contents);
                            break;
                        }
                    }
                }
            }
        }

        if (!commitExists) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        if (!fileExists) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);

        }
    }

    /**
     * Checkout Branch Helper.
     * @param args
     * */
    public static void checkoutBranch(String[] args) {
        Heads headsObj = Utils.readObject(Repository.HEADS, Heads.class);
        if (headsObj.activeBranch.equals(args[1])) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        if (!(headsObj.toActiveHead.containsKey(args[1]))) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (headsObj.toActiveHead.get(args[1]).
                message.equals("initial commit")) {
            ArrayList<String> filesTrackedByCurrentCommit = new ArrayList<>();
            if (!(headsObj.toActiveHead.get(
                    headsObj.activeBranch).message.equals("initial commit"))) {
                filesTrackedByCurrentCommit.addAll(
                        headsObj.toActiveHead.get(headsObj.activeBranch).toBlob.keySet());
                for (int i = 0; i < filesTrackedByCurrentCommit.size(); i++) {
                    File f = Utils.join(Repository.CWD, filesTrackedByCurrentCommit.get(i));
                    if (f.exists()) {
                        f.delete();
                    }
                }
            }
            headsObj.toMaster.clear();
            headsObj.mapCommitMaster(headsObj.toActiveHead.get(args[1]).id,
                    headsObj.toActiveHead.get(args[1]));
            headsObj.activeBranch = args[1];
        } else {
            ArrayList<String> checkedOutFiles = new ArrayList<>();
            checkedOutFiles.addAll(headsObj.
                    toActiveHead.get(args[1]).toBlob.keySet());
            for (int i = 0; i < checkedOutFiles.size(); i++) {
                File f = Utils.join(Repository.CWD, checkedOutFiles.get(i));
                if (f.exists()) {
                    if (headsObj.toActiveHead.get(headsObj.activeBranch)
                            .message.equals("initial commit")) {
                        System.out.println(headsObj.errorMessage);
                        System.exit(0);
                    }
                    if (!(headsObj.toActiveHead.get(headsObj.activeBranch).
                            toBlob.containsKey(checkedOutFiles.get(i)))) {
                        System.out.println(headsObj.errorMessage);
                        System.exit(0);
                    }
                }
            }
            ArrayList<String> filesTrackedByCurrentCommit = new ArrayList<>();
            filesTrackedByCurrentCommit.addAll(
                    headsObj.toActiveHead.get(headsObj.activeBranch).toBlob.keySet());
            for (int i = 0; i < filesTrackedByCurrentCommit.size(); i++) {
                if (!(headsObj.toActiveHead.get(
                        args[1]).toBlob.containsKey(filesTrackedByCurrentCommit.get(i)))) {
                    File f = Utils.join(Repository.CWD, filesTrackedByCurrentCommit.get(i));
                    if (f.exists()) {
                        f.delete();
                    }
                }
            }
            for (Map.Entry<String, Blobs> blobSet
                    : headsObj.toActiveHead.get(args[1]).toBlob.entrySet()) {
                File checkedOut = Utils.join(Repository.CWD, blobSet.getKey());
                if (checkedOut.exists()) {
                    Utils.writeContents(checkedOut, blobSet.getValue().getContents());
                } else {
                    File newCheckedOut = new File(Repository.CWD, blobSet.getKey());
                    Utils.writeContents(newCheckedOut, blobSet.getValue().getContents());
                }
            }
            headsObj.toMaster.clear();
            headsObj.mapCommitMaster(headsObj.toActiveHead.get(args[1]).id,
                    headsObj.toActiveHead.get(args[1]));
            headsObj.activeBranch = args[1];
        }
        headsObj.saveHead(headsObj);
    }



    /** Remove Helper.*/
    public static void removeHelper(String[] args) {
        Staged stagedObj = Staged.readStagingArea();
        Heads headsObj = Utils.readObject(Repository.HEADS, Heads.class);

        Boolean inStagingArea = false;
        Boolean inCurrentCommit = false;

        if (stagedObj.moreToAdd) {
            for (Map.Entry<String, byte[]> addedFilesSet
                    : stagedObj.addedFiles.entrySet()) {
                if (addedFilesSet.getKey().equals(args[1])) {
                    inStagingArea = true;
                    stagedObj.addedFiles.remove(args[1]);
                }
            }
        }

        for (Map.Entry<String, Commit> toMasterHeadSet
                : headsObj.toMaster.entrySet()) {
            if (!(toMasterHeadSet.getValue().message.equals("initial commit"))) {
                for (Map.Entry<String, Blobs> toBlobSet
                        : toMasterHeadSet.getValue().toBlob.entrySet()) {
                    if (toBlobSet.getKey().equals(args[1])) {
                        inCurrentCommit = true;
                        stagedObj.mapRemovedFiles(toBlobSet.getKey(),
                                toBlobSet.getValue().getContents());
                        stagedObj.moreToRemove = true;
                        File f = Utils.join(Repository.CWD, args[1]);
                        if (f.exists()) {
                            f.delete();
                        }
                    }
                }
            }
        }

        if (inStagingArea) {
            if (stagedObj.addedFiles.isEmpty()) {
                stagedObj.moreToAdd = false;
            }
        }

        if (!(inStagingArea) && !(inCurrentCommit)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        stagedObj.saveStage(stagedObj);
    }

    /** Reset
     * */

    public static void resetHelper(String[] args) {
        Heads headsObj = Utils.readObject(Repository.HEADS, Heads.class);

        if (!(headsObj.toAllHeads.containsKey(args[1]))) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        ArrayList<String> fileNames = new ArrayList<>();
        for (Map.Entry<String, Commit> toAllHeadsSet: headsObj.toAllHeads.entrySet()) {
            if (toAllHeadsSet.getKey().equals(args[1])) {
                fileNames.addAll(toAllHeadsSet.getValue().toBlob.keySet());
                headsObj.mapActiveHead(toAllHeadsSet.getValue());
            }
        }

        for (int i = 0; i < fileNames.size(); i++) {
            String[] A = {"checkout", args[1], "--", fileNames.get(i)};
            checkoutGivenID(A);
        }
        headsObj.saveHead(headsObj);
    }



    /**
     * log Helper.
     * */
    public static void logHelper(String[]args) {

        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        String tempParent = "";
        Heads headsObj = readObject(Repository.HEADS, Heads.class);
        for (Map.Entry<String, Commit> toActiveHeadSet
                : headsObj.toActiveHead.entrySet()) {
            if (toActiveHeadSet.getKey().equals(headsObj.activeBranch)) {
                tempParent = toActiveHeadSet.getValue().getParent();
                System.out.println("===");
                System.out.println("commit " + toActiveHeadSet.getValue().getId());
                System.out.println("Date: " + DATEFORMAT.format(
                        toActiveHeadSet.getValue().getTimestamp()));
                System.out.println(toActiveHeadSet.getValue().getMessage());
            }
        }
        while (tempParent != null) {
            System.out.println();
            System.out.println("===");
            System.out.println("commit " + headsObj.toAllHeads.get(tempParent).id);
            System.out.println("Date: " + DATEFORMAT.format(
                    headsObj.toAllHeads.get(tempParent).timestamp));
            System.out.println(headsObj.toAllHeads.get(tempParent).getMessage());
            tempParent = headsObj.toAllHeads.get(tempParent).getParent();
        }

    }

    /** global log. */
    public static void globalLogHelper() {

        Heads headsObj = readObject(Repository.HEADS, Heads.class);
        for (Map.Entry<String, Commit> toAllCommitsSet: headsObj.toAllHeads.entrySet()) {
            System.out.println("===");
            System.out.println("commit " + toAllCommitsSet.getValue().getId());
            System.out.println("Date: " + DATEFORMAT.format(
                    toAllCommitsSet.getValue().getTimestamp()));
            System.out.println(toAllCommitsSet.getValue().getMessage());
            System.out.println();
        }
    }

    /** Helper to find ID. * */
    public static void findHelper(String[] args) {

        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        Boolean commitMessageExists = false;
        Heads headsObj = readObject(Repository.HEADS, Heads.class);
        for (Map.Entry<String, Commit> toAllCommitsSet: headsObj.toAllHeads.entrySet()) {
            if (toAllCommitsSet.getValue().message.equals(args[1])) {
                commitMessageExists = true;
                System.out.println(toAllCommitsSet.getValue().id);
            }
        }
        if (!commitMessageExists) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }


    /** Save the Commit Object as a File in the Commits Folder */
    public static void saveCommit(Commit newlyCommitted) {
        File newCommitFile = Utils.join(Repository.COMMITS, newlyCommitted.id);
        Utils.writeObject(newCommitFile, newlyCommitted);
    }

    /**
     * Maps File Name to Blob Object.
     * */
    public void mapBlob(String fileName, Blobs blobToAdd) {
        toBlob.put(fileName, blobToAdd);
    }

    /**
     * Getter Method for Commit Message.
     * @return  message
     * */
    public String getMessage() {
        return message;
    }

    /**
     * Getter Method for Commit TimeStamp.
     * @return timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Getter Method for Commit ID.
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Getter Method for Commit Parent.
      * @return parent
     */
    public String getParent() {
        return parent;
    }
}

