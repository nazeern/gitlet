package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Nitin Nazeer
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        Repo repo = new Repo();
        if (args.length == 0) {
            exitWithError("Please enter a command.");
        }
        if (args[0].equals("init")) {
            validateNumArgs(args, 0);
            repo.init();
            return;
        }
        if (!(new File(repo.CWD, ".gitlet")).isDirectory()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        switch (args[0]) {
        case "display":
            repo.display();
            break;
        case "add":
            validateNumArgs(args, 1);
            repo.add(args[1]);
            break;
        case "commit":
            validateNumArgs(args, 1);
            repo.commit(args[1]);
            break;
        case "checkout":
            runCheckout(repo, args);
            break;
        case "log":
            repo.log();
            break;
        case "global-log":
            repo.globalLog();
            break;
        case "rm":
            validateNumArgs(args, 1);
            repo.rm(args[1]);
            break;
        case "find":
            repo.find(args[1]);
            break;
        case "status":
            repo.status();
            break;
        case "branch":
            repo.branch(args[1]);
            break;
        case "rm-branch":
            repo.rmBranch(args[1]);
            break;
        case "reset":
            repo.reset(args[1]);
            break;
        case "merge":
            repo.merge(args[1]);
            break;
        default:
            exitWithError("No command with that name exists.");
        }
    }

    /** @param args the args passed into the command line.
     * @param n the number of arguments expected.
     *
     * Ensures the correct number of arguments are passed in. */
    public static void validateNumArgs(String[] args, int n) {
        if ((args.length - 1) != n) {
            exitWithError("Incorrect operands.");
        }
    }

    /**
     *
     * @param repo the active REPO object that manage the repository.
     * @param args the args passed into the command line.
     */
    public static void runCheckout(Repo repo, String[] args) {
        if (args.length == 2) {
            repo.checkoutBranch(args[1]);
        } else if (args.length == 3) {
            if (!args[1].equals("--")) {
                exitWithError("Incorrect operands.");
            }
            repo.checkout(args[2]);
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                exitWithError("Incorrect operands.");
            }
            repo.checkout(args[1], args[3]);
        } else {
            exitWithError("Incorrect operands.");
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
