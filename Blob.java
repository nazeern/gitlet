package gitlet;

import java.io.File;
import java.io.Serializable;

/** The Blob class representing file contents.
 * @author Nitin Nazeer
 */
public class Blob implements Serializable {

    /** The string contents of a file. */
    private String contents;

    /** @param givenContents the contents of the blob. */
    public Blob(String givenContents) {
        this.contents = givenContents;
    }

    /** Return the contents of the blob. */
    public String getContents() {
        return this.contents;
    }

    /** Write the commit to a file named by the blob's hashcode in the blobs
     * directory.*/
    public void saveBlob() {
        File newBlob = Utils.join(Repo.BLOBS_DIR, this.code());
        Utils.writeObject(newBlob, this);
    }

    /** Returns the hashcode of the current blob. */
    public String code() {
        return Utils.sha1(this.contents);
    }

}
