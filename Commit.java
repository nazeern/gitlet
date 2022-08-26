package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/** The Commit class representing a gitlet commit object.
 * @author Nitin Nazeer
 */
public class Commit implements Serializable {

    /** The commit message. */
    private String message;

    /** The commit timestamp. */
    private ZonedDateTime timestamp;

    /** The commit's tracked files.
     * Keys: filenames, e.g. wug.txt
     * Values: hashcode to a blob */
    private HashMap<String, String> tracked;

    /** The hashcode of the parent commit. */
    private String parent;

    /** Create a commit w/ message and parent. Initialize timestamp and tracked
     * files.
     *
     * @param givenMessage the given message for the commit.
     * @param givenParent the hashcode of the current commit's parent. */
    public Commit(String givenMessage, String givenParent) {
        this.message = givenMessage;
        this.parent = givenParent;
        this.timestamp = ZonedDateTime.now(ZoneId.systemDefault());
        this.tracked = new HashMap<String, String>();
    }

    /** Create a new commit that is a copy of head. The tracked files are a
     * shallow copy of the head's tracked files.
     *
     * @param givenHead the head to make a copy of.
     * @param givenMessage the commit message.
     * @param givenParent the hashcode of the current commit's parent. */
    @SuppressWarnings("unchecked")
    public Commit(String givenMessage, String givenParent, Commit givenHead) {
        this.message = givenMessage;
        this.parent = givenParent;
        this.timestamp = ZonedDateTime.now(ZoneId.systemDefault());
        this.tracked = (HashMap<String, String>) givenHead.tracked.clone();
    }

    /** The initial commit. */
    public Commit() {
        this.message = "initial commit";
        this.parent = null;
        this.tracked = new HashMap<String, String>();
        this.timestamp = theEpoch();
    }

    /** Returns the hashmap of tracked files. */
    public HashMap<String, String> getTracked() {
        return this.tracked;
    }

    /** Write the commit to a file named by the commit's hashcode in the commits
     * directory. */
    public void saveCommit() {
        File newCommit = Utils.join(Repo.COMMIT_DIR, this.code());
        Utils.writeObject(newCommit, this);
    }

    /** For this commit, get the associated blob of a given filename. If the
     * file is not tracked, return null.
     *
     * @param filename the filename to retrieve the blob for. */
    public Blob getBlob(String filename) {
        String blobCode = this.tracked.get(filename);
        if (blobCode == null) {
            return null;
        }
        File f = Utils.join(Repo.BLOBS_DIR, blobCode);
        if (f.exists()) {
            return Utils.readObject(f, Blob.class);
        }
        return null;
    }

    /** Returns the commit message. */
    public String getMessage() {
        return this.message;
    }

    /** Returns the timestamp. */
    public ZonedDateTime getTimestamp() {
        return this.timestamp;
    }

    /** Returns the timestamp string representation. */
    public String getTimestampRepr() {
        String pattern = "E LLL dd HH:mm:ss y xx";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return this.timestamp.format(formatter);
    }

    /** Returns the parent commit of the current commit. */
    public Commit getParent() {
        if (this.parent == null) {
            return null;
        }
        return getCommit(this.parent);
    }

    /** Returns the commit with given hashcode.
     *
     * @param code the hashcode of the commit to retrieve. */
    public Commit getCommit(String code) {
        if (code.equals(null)) {
            return null;
        }
        File f = Utils.join(Repo.COMMIT_DIR, code);
        if (f.exists()) {
            return Utils.readObject(f, Commit.class);
        }
        System.out.println("commit " + code + " not found.");
        return null;
    }

    /** Returns the Java Epoch. */
    public ZonedDateTime theEpoch() {
        final LocalDateTime ldt = LocalDateTime.of(1970,
                Month.JANUARY,
                1,
                0,
                0,
                0);
        ZoneId timezone = ZoneId.systemDefault();
        return ZonedDateTime.of(ldt, timezone).minusHours(8);
    }

    /** Returns the hashcode of the current commit. */
    public String code() {
        return Utils.sha1(this.message + this.parent
                + this.getTimestampRepr());
    }

    @Override
    public String toString() {
        String msg = "Tracking: ";
        for (String filename : this.tracked.keySet()) {
            msg += filename + "\n";
        }
        return msg;
    }
}
