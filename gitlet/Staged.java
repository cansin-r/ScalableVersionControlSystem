package gitlet;
import java.io.Serializable;
import java.util.*;

/**
This class creates objects that we stage in the
 Staging Area before Commiting
 or Removing the file entirely from
 the .gitlet repository
 @author Cansin Rodoplu
 */

public class Staged implements Serializable {


    /** Hash Map mapping from the name of the file the
     * user is staging for addition to its contents*/
    HashMap<String, byte[]> addedFiles;

    /** HashMap mapping from the name of the file
     * the user is staging for removal to its contents*/
    HashMap<String, byte[]> removedFiles;

    /** Boolean indicating if there are files to be committed*/
    Boolean moreToAdd;

    /** Boolean indicating if there are files to be removed*/
    Boolean moreToRemove;


    public Staged() {

        addedFiles = new HashMap<>();
        removedFiles = new HashMap<>();
        moreToAdd = false;
        moreToRemove = false;

    }

    /** helper methods for the hashmaps */


    /** Insert Elements into added_files map */
    public HashMap mapAddedFiles(String fileName, byte[] contents) {
        addedFiles.put(fileName, contents);
        moreToAdd = true;
        return addedFiles;
    }
    /** Insert Elements into remove_files map*/
    public HashMap mapRemovedFiles(String fileName, byte[] contents) {
        removedFiles.put(fileName, contents);
        moreToRemove = true;
        return removedFiles;
    }

    /** Reads and returns the Staged object in the Staging Area file*/
    public static Staged readStagingArea() {
        return Utils.readObject(Repository.STAGINGAREA, Staged.class);
    }

    public void saveStage(Staged stagedObj) {
        Utils.writeObject(Repository.STAGINGAREA, stagedObj);
    }
}
