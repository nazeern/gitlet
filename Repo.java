package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import static gitlet.Utils.error;

/** A Repo class representing all the data structures and methods associated
 * with a gitlet repository.
 *
 * @author Nitin Nazeer
 */
public class Repo {

    /** The file object repr of the current working directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** The file object repr of the .gitlet directory. */
    static final File GITLET_DIR = Utils.join(CWD, ".gitlet");

    /** The file object repr of the commits directory. */
    static final File COMMIT_DIR = Utils.join(GITLET_DIR, "commits");

    /** The file object repr of the blobs directory. */
    static final File BLOBS_DIR = Utils.join(GITLET_DIR, "blobs");

    /** The file object repr of the add file. */
    static final File ADD_FILE = Utils.join(GITLET_DIR, "add");

    /** The file object repr of the remove file. */
    static final File REM_FILE = Utils.join(GITLET_DIR, "remove");

    /** The file object repr of the branches file. */
    static final File BRANCHES_FILE = Utils.join(GITLET_DIR, "branches");

    /** The file object repr of the active file. */
    static final File ACTIVE_FILE = Utils.join(GITLET_DIR, "active");

    /** The file object repr of the HEAD file. */
    static final File HEAD_FILE = Utils.join(GITLET_DIR, "head");

    /** A hashmap that represents the staging area for addition.
     * Keys: filename to be committed
     * Values: hashcode of the file's associated blob. */
    private HashMap<String, String> addition;

    /** A hashmap that represents the staging area for addition.
     * Keys: filename to be committed
     * Values: hashcode of the file's associated blob. */
    private ArrayList<String> removal;

    /** hashcode that points to the HEAD commit. */
    private String head;

    /** Name of the currently active branch. */
    private String activeBranch;

    /** hashmap that stores branch names and the corresponding hashmap. */
    private HashMap<String, String> branches;

    /** Ensures .gitlet exists, then loads addition, removal, HEAD, and master.
     * */
    public Repo() {
        File probe = Utils.join(CWD, ".gitlet");
        if (probe.exists() && probe.isDirectory()) {
            loadData();
        }
    }

    /** Return the hash of the active branch. */
    public String getActiveBranchHash() {
        return this.branches.get(this.activeBranch);
    }

    /** Given a hashcode stub, finds and returns the matching hashcode from the
     * commits directory. Returns null if not found.
     *
     * @param stub An abbreviated version of a hashcode. */
    public String findHash(String stub) {
        for (String filename : Utils.plainFilenamesIn(COMMIT_DIR)) {
            if (filename.contains(stub)) {
                return filename;
            }
        }
        return null;
    }

    /** Returns the HEAD commit, otherwise returns null.*/
    public Commit getHead() {
        File f = Utils.join(COMMIT_DIR, this.head);
        if (f.exists()) {
            return Utils.readObject(f, Commit.class);
        }
        System.out.println("head not found");
        return null;
    }

    /** Returns the master commit, otherwise returns null.*/
    public Commit getActiveBranch() {
        File f = Utils.join(COMMIT_DIR, getActiveBranchHash());
        if (f.exists()) {
            return Utils.readObject(f, Commit.class);
        }
        System.out.println("Active branch not found.");
        return null;
    }

    /** Gets the commit with the given hashcode, returns null if it
     * doesn't exist.
     *
     * @param code the hashcode of the commit, possibly abbreviated. */
    public Commit getCommit(String code) {
        if (code == null) {
            return null;
        }
        File f = Utils.join(COMMIT_DIR, code);
        if (f.exists()) {
            return Utils.readObject(f, Commit.class);
        }
        return null;
    }

    /** loadData from the .gitlet folder. addition hashmap, removal hashmap
     * HEAD, and master. Assumes .gitlet exists. */
    @SuppressWarnings("unchecked")
    public void loadData() {
        this.addition = Utils.readObject(ADD_FILE, HashMap.class);
        this.removal = Utils.readObject(REM_FILE, ArrayList.class);
        this.head = Utils.readObject(HEAD_FILE, String.class);
        this.activeBranch = Utils.readObject(ACTIVE_FILE, String.class);
        this.branches = Utils.readObject(BRANCHES_FILE, HashMap.class);
    }

    /** If already a repo, errors and prints message.
     *
     * make .gitlet, make addition and removal hashmaps, make commits and blobs
     * directories create commit 0, set master and HEAD. */
    public void init() throws IOException {
        File probe = Utils.join(CWD, ".gitlet");
        if (probe.exists() && probe.isDirectory()) {
            exitWithError("A Gitlet version-control system already exists in "
                    + "the current directory.");
        }


        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOBS_DIR.mkdir();



        Commit initial = new Commit();
        initial.saveCommit();


        this.activeBranch = "master";
        this.branches = new HashMap<String, String>();
        this.branches.put(this.activeBranch, initial.code());
        this.head = initial.code();

        Utils.writeObject(BRANCHES_FILE, this.branches);
        Utils.writeObject(HEAD_FILE, this.head);
        Utils.writeObject(ACTIVE_FILE, this.activeBranch);


        this.addition = new HashMap<String, String>();
        this.removal = new ArrayList<String>();


        Utils.writeObject(ADD_FILE, this.addition);
        Utils.writeObject(REM_FILE, this.removal);

    }

    /** Check that the file exists. Then, compare the current file w/ the latest
     * HEAD version of the file.
     *
     * If the same, remove that file from the staging
     * area.
     *
     * If different, make a blob for the file, and add it to staging hashmap.
     *
     * @param filename the name of the file to be added. */
    public void add(String filename) {
        File file = new File(Utils.join(CWD), filename);
        if (!file.exists() || !file.isFile()) {
            exitWithError("File does not exist.");
        }
        String currFileContents = Utils.readContentsAsString(file);

        Commit curr = getHead();



        if (curr.getTracked().containsKey(filename)) {
            if (Utils.sha1(currFileContents).equals(
                    curr.getTracked().get(filename))) {
                this.addition.remove(filename);
                this.removal.remove(filename);
                Utils.writeObject(ADD_FILE, this.addition);
                Utils.writeObject(REM_FILE, this.removal);
                return;
            }
        }




        Blob newBlob = new Blob(currFileContents);
        newBlob.saveBlob();
        this.addition.put(filename, newBlob.code());
        this.removal.remove(filename);
        Utils.writeObject(ADD_FILE, this.addition);
        Utils.writeObject(REM_FILE, this.removal);


    }

    /** Shallow copy the HEAD commit. The next commit's parent is the curr head,
     * and its tracked contains the previous commit's files updated with the
     * files from the staging area.
     *
     * @param message the provided commit message. */
    public void commit(String message) {
        if (this.addition.isEmpty() && this.removal.isEmpty()) {
            exitWithError("No changes added to the commit.");
        }
        if (message.equals("")) {
            exitWithError("Please enter a commit message.");
        }

        Commit curr = getHead();




        Commit next = new Commit(message, curr.code(), curr);



        for (String filename : this.addition.keySet()) {
            next.getTracked().put(filename, this.addition.get(filename));
        }
        this.addition.clear();


        for (String filename : this.removal) {
            next.getTracked().remove(filename);
        }
        this.removal.clear();


        next.saveCommit();
        this.branches.put(this.activeBranch, next.code());
        this.head = next.code();


        Utils.writeObject(BRANCHES_FILE, this.branches);
        Utils.writeObject(HEAD_FILE, this.head);
        Utils.writeObject(ADD_FILE, this.addition);
        Utils.writeObject(REM_FILE, this.removal);
    }

    /** basic checkout with no hashcode. If file is not tracked, throw error.
     *
     * @param filename the provided filename to checkout. */
    public void checkout(String filename) {
        Commit curr = getHead();
        Blob currBlob = curr.getBlob(filename);
        if (currBlob == null) {
            throw error("File does not exist in that commit.");
        }
        File currFile = Utils.join(CWD, filename);
        Utils.writeContents(currFile, currBlob.getContents());
    }

    /** checkout to commit w given hashcode. If no commit exists or the commit
     * does not track the file, throw an error.
     *
     * @param commitId the hashcode of the commit to checkout to.
     * @param filename the name of file that will be checked out. */
    public void checkout(String commitId, String filename) {
        Commit curr = getCommit(findHash(commitId));
        if (curr == null) {
            exitWithError("No commit with that id exists.");
        }
        Blob currBlob = curr.getBlob(filename);
        if (currBlob == null) {
            exitWithError("File does not exist in that commit.");
        }
        File currFile = Utils.join(CWD, filename);
        Utils.writeContents(currFile, currBlob.getContents());
    }

    /** Checkout of a full branch.
     *
     * @param newBranchName the name of the branch to checkout.
     */
    public void checkoutBranch(String newBranchName) {




        for (String filename : Utils.plainFilenamesIn(CWD)) {
            if (filename.contains(".txt")
                    && !getActiveBranch().getTracked().containsKey(filename)) {
                exitWithError("There is an untracked file in the way; delete "
                        + "it or add and commit it first.");
            }
        }


        if (!this.branches.containsKey(newBranchName)) {
            exitWithError("No such branch exists.");
        }



        if (this.activeBranch.equals(newBranchName)) {
            exitWithError("No need to checkout the current branch.");
        }


        Commit newBranch = getCommit(this.branches.get(newBranchName));
        for (String filename : Utils.plainFilenamesIn(CWD)) {
            if (filename.contains(".txt")) {
                Utils.restrictedDelete(filename);
            }
        }

        for (String filename : newBranch.getTracked().keySet()) {
            checkout(this.branches.get(newBranchName), filename);
        }



        this.head = this.branches.get(newBranchName);
        this.activeBranch = newBranchName;
        Utils.writeObject(HEAD_FILE, this.head);
        Utils.writeObject(ACTIVE_FILE, this.activeBranch);


        this.addition.clear();
        this.removal.clear();
        Utils.writeObject(ADD_FILE, this.addition);
        Utils.writeObject(REM_FILE, this.removal);
    }

    /** Unstage file if currently staged for addition. If file currently
     * tracked, stage for removal, remove from working directory.
     *
     * @param filename the filename to be removed. */
    public void rm(String filename) {
        Commit curr = getHead();


        if (!this.addition.containsKey(filename)
                && !curr.getTracked().containsKey(filename)) {
            exitWithError("No reason to remove the file.");
        }


        if (this.addition.containsKey(filename)) {
            this.addition.remove(filename);
        }



        if (curr.getTracked().containsKey(filename)) {
            this.removal.add(filename);
            File delFile = Utils.join(CWD, filename);
            Utils.restrictedDelete(delFile);
        }



        Utils.writeObject(ADD_FILE, this.addition);
        Utils.writeObject(REM_FILE, this.removal);
    }

    /** Display log information about all commits ever made, order doesn't
     * matter. */
    public void globalLog() {
        for (String filename : Utils.plainFilenamesIn(COMMIT_DIR)) {
            Commit curr = getCommit(filename);
            String msg = "";
            msg += "===\n";
            msg += "commit " + curr.code() + "\n";
            msg += "Date: "
                    + curr.getTimestampRepr()
                    + "\n";
            msg += curr.getMessage() + "\n";
            System.out.println(msg);
        }
    }

    /** Print hashcode of all commits with the given message.
     *
     * @param message the message to find. */
    public void find(String message) {
        boolean found = false;
        for (String filename : Utils.plainFilenamesIn(COMMIT_DIR)) {
            Commit curr = getCommit(filename);
            if (curr.getMessage().equals(message)) {
                System.out.println(filename);
                found = true;
            }
        }
        if (!found) {
            exitWithError("Found no commit with that message.");
        }
    }

    /** Print the branches and the files staged for addition and removal. */
    public void status() {
        System.out.println("=== Branches ===");
        Object[] branchKeys = this.branches.keySet().toArray();
        Arrays.sort(branchKeys);
        for (Object branchName : branchKeys) {
            if (this.activeBranch.equals(branchName)) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        Object[] addKeys = this.addition.keySet().toArray();
        Arrays.sort(addKeys);
        for (Object filename : addKeys) {
            System.out.println(filename);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        Object[] remKeys = this.removal.toArray();
        Arrays.sort(remKeys);
        for (Object filename : remKeys) {
            System.out.println(filename);
        }
        System.out.println();

        Commit curr = getHead();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String filename : Utils.plainFilenamesIn(CWD)) {
            if (filename.contains(".txt")) {
                File fileObj = Utils.join(CWD, filename);
                String currFileContents = Utils.readContentsAsString(fileObj);
                if (curr.getTracked().containsKey(filename)
                      &&  !Utils.sha1(currFileContents).equals(
                        curr.getTracked().get(filename))) {
                    System.out.println(filename + " (modified)");
                }
            }
        }
        for (String filename : curr.getTracked().keySet()) {
            if (!Utils.plainFilenamesIn(CWD).contains(filename)
                && !this.removal.contains(filename)) {
                System.out.println(filename + " (deleted)");
            }
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for (String filename : Utils.plainFilenamesIn(CWD)) {
            if (filename.contains(".txt")) {
                if (!this.addition.containsKey(filename)
                        && !curr.getTracked().containsKey(filename)) {
                    System.out.println(filename);
                }
            }
        }
        System.out.println();
    }

    /** Create a new branch with BRANCHNAME that points at HEAD.  */
    public void branch(String branchName) {
        if (this.branches.containsKey(branchName)) {
            exitWithError("A branch with that name already exists.");
        }
        this.branches.put(branchName, this.head);
        Utils.writeObject(BRANCHES_FILE, this.branches);
    }

    /** Remove the branch with the given BRANCHNAME.
     *
     * @param branchName the name of the branch to be removed.
     */
    public void rmBranch(String branchName) {
        if (this.activeBranch.equals(branchName)) {
            exitWithError("Cannot remove the current branch.");
        }
        if (!this.branches.containsKey(branchName)) {
            exitWithError("A branch with that name does not exist.");
        }
        this.branches.remove(branchName);
        Utils.writeObject(BRANCHES_FILE, this.branches);
    }

    /** Resets the state to the commit of the given COMMITID.
     *
     * @param commitId the hashcode of the commit to reset to.
     */
    public void reset(String commitId) {
        Commit curr = getHead();
        for (String filename : Utils.plainFilenamesIn(CWD)) {
            if (filename.contains(".txt")
                    && (!getActiveBranch().getTracked().containsKey(filename)
                    && !this.addition.containsKey(filename))) {
                exitWithError("There is an untracked file in the way; delete "
                        + "it, or add and commit it first.");
            }
        }

        commitId = findHash(commitId);

        Commit prevCommit = getCommit(commitId);
        if (prevCommit == null) {
            exitWithError("No commit with that id exists.");
        }



        for (String filename : Utils.plainFilenamesIn(CWD)) {
            if (filename.contains(".txt")) {
                Utils.restrictedDelete(filename);
            }
        }

        for (String filename : prevCommit.getTracked().keySet()) {
            checkout(commitId, filename);
        }



        this.head = commitId;
        this.branches.put(this.activeBranch, commitId);
        Utils.writeObject(HEAD_FILE, this.head);
        Utils.writeObject(BRANCHES_FILE, this.branches);


        this.addition.clear();
        this.removal.clear();
        Utils.writeObject(ADD_FILE, this.addition);
        Utils.writeObject(REM_FILE, this.removal);
    }

    /** Check for errors to catch in merge.
     *
     * @param otherBranchName the name of the branch to merge with. */
    public void checkMerge(String otherBranchName) {
        if (this.activeBranch.equals(otherBranchName)) {
            exitWithError("Cannot merge a branch with itself.");
        }
        if (!this.branches.containsKey(otherBranchName)) {
            exitWithError("A branch with that name does not exist.");
        }
        Commit curr = getHead();
        for (String filename : Utils.plainFilenamesIn(CWD)) {
            if (filename.contains(".txt")
                    && (!getActiveBranch().getTracked().containsKey(
                    filename))) {
                if (this.addition.containsKey(filename)) {
                    exitWithError("You have uncommitted changes.");
                } else {
                    exitWithError("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                }
            }
        }
    }

    /** Merges two branches together according to the Git spec.
     *
     * @param otherBranchName the name of the branch to merge with.
     */
    public void merge(String otherBranchName) {

        checkMerge(otherBranchName);

    }

    /** Print out the commit history from the head. */
    public void log() {
        Commit curr = getHead();
        while (curr != null) {
            System.out.println("===");
            System.out.println("commit " + curr.code());
            System.out.println("Date: " + curr.getTimestampRepr());
            System.out.println(curr.getMessage());
            System.out.println();
            curr = curr.getParent();
        }
    }

    /**
     * For each branch, display all commits in reverse order.
     * */
    public void display() {
        for (String branchName: this.branches.keySet()) {
            Commit curr = getCommit(this.branches.get(branchName));
            while (curr != null) {
                System.out.println("===");
                System.out.println("commit " + curr.code());
                System.out.println(curr.getMessage());
                System.out.print("tracking: ");
                for (String filename : curr.getTracked().keySet()) {
                    System.out.println(filename + ", ");
                }
                System.out.println();
                curr = curr.getParent();
            }
        }
    }

    /**
     *
     * @param message the error message to exit with.
     */
    public static void exitWithError(String message) {
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
        System.exit(0);
    }

}
