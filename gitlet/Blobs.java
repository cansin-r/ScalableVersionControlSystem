package gitlet;
import java.io.Serializable;

/**
 * Blobs class creates Blob Objects which are stored in the Commit Object.
 * @author Cansin Rodoplu
 */
public class Blobs implements Serializable {


    /**
    Private Instances
     */
    /** The Blob Hash ID. */
    private String blobId;
     /** The name of the file that is added. */
    private String blobName;
    /** Contents of the file that is added.*/
    private byte[] contents;


    /**
     * Blob Object Constructor.*/
    public Blobs(String name, byte[] contents) {
        blobId = Utils.sha1(contents);
        blobName = name;
        this.contents = contents;
    }

    /** getter methods. */
    public String getBlobId() {
        return blobId;
    }

    public String getBlobName() {
        return blobName;
    }

    public byte[] getContents() {
        return contents;
    }
}
