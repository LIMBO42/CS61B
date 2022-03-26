package gitlet;



import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command. ");
            return;
        }
        switch (args[0]) {
            case "init":
                Init();
                break;
            case "add":
                Add(args[1]);
                break;
            case "commit":
                if (args.length <2) System.out.println("lease enter a commit message.");
                Commit(args[1], null);
                break;
            case "checkout":
                if (args[1].equals("--")) {CheckoutSingleFile((String) null, args[2]);}
                else if (args.length > 2 && args[2].equals("--")) {CheckoutSingleFile(args[1], args[3]);}
                else if (args.length == 2) { CheckoutBranch(args[1]); }
                else { System.out.println("Incorrect operands."); }
                break;
            case "log":
                Log();
                break;
            case "global-log":
                GlobalLog();
                break;
            case "rm":
                RM(args[1]);
                break;
            case "find":
                Find(args[1]);
                break;
            case "status":
                Status();
                break;
            case "branch":
                Branch(args[1]);
                break;
            case "rm-branch":
                RMBranch(args[1]);
                break;
            case "reset":
                Reset(args[1]);
                break;
            case "merge":
                Merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }

    static void Init() {
        File rootDir = new File(_rootDirPath);
        if (rootDir.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        }
        rootDir.mkdir();

        File logsDir = new File(_logsDirPath);
        if (!logsDir.exists()) {
            logsDir.mkdir();
        }

        File blobDir = new File(_blobDirPath);
        if (!blobDir.exists()) {
            blobDir.mkdir();
        }

        File stagedDir = new File(_stagedDirPath);
        if (!stagedDir.exists()) {
            stagedDir.mkdir();
        }

        _head = new Commit();
        _branchStatus = new BranchStatus();

        writeCommit(_head);
        writeBranchStatus();
    }

    static void Add(String fileName) throws IOException {
        // read branchStatus and head commit
        if (!readBranchStatusAndHead()) return;

        // create the File object
        File addFile = new File(fileName);
        if (!addFile.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        String addFileSHA = Utils.sha1(Utils.readContents(addFile), fileName);
        File blobFile = new File(_stagedDirPath+File.separator+addFileSHA);
        // if the addedfile is the same as the current commit?
        if (!addFileSHA.equals(_head.tree().get(fileName))) {
            // if not, has it been previously staged?
            if (_branchStatus.addList().contains(fileName))  {
                if (_branchStatus.stageMap().get(fileName) != addFileSHA) {
                    // if does, delete the old staged blob
                    deleteBlob(fileName);
                }
                // if the staged blob has not been updated, do nothing
                else {
                    return;
                }
            }
            else {
                _branchStatus.addList().add(fileName);
            }
            // if the file has been staged and updated or hasn't been staged at all, add the new blob
            _branchStatus.stageMap().put(fileName, addFileSHA);
//            blobFile.createNewFile();
            copyFile(addFile, blobFile);
        }
        // if the added file is the same as the current commit
        else {
            // if the file has been previously staged for modification, clear the stage record
            if (_branchStatus.addList().contains(fileName)) {
                deleteBlob(fileName);
                _branchStatus.addList().remove(fileName);
                // restore the stageMap to the last commit state for this file
                _branchStatus.stageMap().put(fileName, addFileSHA);
            }
            // if the file has been previously staged for removal, clear the stage record
            else if (_branchStatus.rmList().contains(fileName)) {
                _branchStatus.rmList().remove(fileName);
                _branchStatus.stageMap().put(fileName, addFileSHA);
            }
        }
        writeBranchStatus();
    }

    static void Commit(String message, String branchName) throws IOException {
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (!readBranchStatusAndHead()) return;
        if (_branchStatus.addList().isEmpty() && _branchStatus.rmList().isEmpty()) {
            System.out.println("No changes added to the commit");
            return;
        }

        Commit newCommit;
        if (branchName == null) {
            newCommit = new Commit(message, _branchStatus.getBranchTable(), _branchStatus.stageMap());
        }
        else {
            newCommit = new Commit(message, _branchStatus.getBranchTable(), _branchStatus.getBranchTable(branchName), _branchStatus.stageMap());
        }
        writeCommit(newCommit);
        for (String filename: _branchStatus.addList()) {
            String filesha = _branchStatus.stageMap().get(filename);
            File stage = new File(_stagedDirPath + File.separator + filesha);
            File blob = new File(_blobDirPath + File.separator + filesha);
            // If the staged file is EXACTLY the same as the one in the blob (add after remove/checkout)
            if (blob.exists()) {
                continue;
            }
            copyFile(stage, blob);
            deleteBlob(filename);
        }
//        _branchStatus.stageMap().clear();
        _branchStatus.addList().clear();
        _branchStatus.rmList().clear();
        writeBranchStatus();
    }

    static void CheckoutSingleFile(String commitID, String filename) throws IOException {
        List<Object> commitInfo = FindCommitID(commitID);
        if (commitInfo == null) {
            return;
        }
        Commit commit = (Commit) commitInfo.get(0);
        CheckoutSingleFile(commit, filename);
    }

    static void CheckoutSingleFile(Commit commit, String filename) throws IOException {
        if (!commit.tree().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File originFile = new File(filename);
        Utils.restrictedDelete(filename);
        File newFile = new File(_blobDirPath + File.separator + commit.tree().get(filename));
        copyFile(newFile, originFile);
        return;
    }

    static void Log() {
        if (!readBranchStatusAndHead()) return;
        File commitFile = new File(_logsDirPath+File.separator+_branchStatus.getBranchTable());
        Commit commit = _head;
        while (commit.getParent() != null) {
            printCommit(commitFile, commit);
            commitFile = new File(_logsDirPath + File.separator + commit.getParent());
            commit = Utils.readObject(commitFile, Commit.class);
        }
        printCommit(commitFile, commit);
    }

    static void GlobalLog() {
        File commits = new File(_logsDirPath);
        Commit commit = null;
        for (File f: commits.listFiles()) {
            commit = Utils.readObject(f, Commit.class);
            printCommit(f, commit);
        }
    }

    static void Find(String message) {
        File commits = new File(_logsDirPath);
        Commit commit = null;
        boolean find = false;
        for (File f: commits.listFiles()) {
            commit = Utils.readObject(f, Commit.class);
            if (message.equals(commit.getMessage())) {
                System.out.println(f.getName());
                find = true;
            }
        }
        if (!find) {
            System.out.println("Found no commit with that message.");
            return;
        }
    }

    static void RM(String fileName) {
        if (!readBranchStatusAndHead()) return;
        // if the file has been staged for adding, unstage it
        if (_branchStatus.addList().contains(fileName)) {
            deleteBlob(fileName);
            _branchStatus.stageMap().remove(fileName);
            _branchStatus.addList().remove(fileName);
        }
        // if the file has been tracked in the current commit, stage it for removal
        else if (_head.tree().containsKey(fileName) && _branchStatus.stageMap().containsKey(fileName)) {
            _branchStatus.stageMap().remove(fileName);
            _branchStatus.rmList().add(fileName);
            File WDFile = new File(fileName);
            if (WDFile.exists()) {
                Utils.restrictedDelete(WDFile);
            }
        }
        else {
            System.out.println("No reason to remove the file.");
            return;
        }
        writeBranchStatus();
    }

    static void Status() {
        if (!readBranchStatusAndHead()) return;
        System.out.println("=== Branches ===");
        for (String key: _branchStatus.branchTable().keySet()) {
            if (key.equals(_branchStatus.getBranchCur())) {
                System.out.println("*"+key);
            }
            else {
                System.out.println(key);
            }
        }

        System.out.println("\n=== Staged Files ===");
        Collections.sort(_branchStatus.addList());
        for (String fileName: _branchStatus.addList()) {
            System.out.println(fileName);
        }

        System.out.println("\n=== Removed Files ===");
        Collections.sort(_branchStatus.rmList());
        for (String fileName:_branchStatus.rmList()) {
            System.out.println(fileName);
        }

        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===");
    }

    static void Branch(String branchName) {
        if (!readBranchStatusAndHead()) return;
        if (_branchStatus.containsKeyBranchTable(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        _branchStatus.splitPoints().add(_branchStatus.getBranchTable());
        _branchStatus.setBranchTable(branchName, _branchStatus.getBranchTable());
        writeBranchStatus();
    }

    static void CheckoutBranch(String branchName) throws IOException {
        if (!readBranchStatusAndHead()) return;
        if (_branchStatus.getBranchCur().equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        else if (!_branchStatus.containsKeyBranchTable(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }

        // delete all files that have been tracked in the current branch
        for (String fileName: _head.tree().keySet()) {
            Utils.restrictedDelete(fileName);
        }

        // Copy files from the new commit into WD
        File checkedCommitFile = new File(_logsDirPath+File.separator+_branchStatus.getBranchTable(branchName));
        Commit checkedCommit = Utils.readObject(checkedCommitFile, Commit.class);
        CheckoutFiles(checkedCommit);
//        for (Map.Entry<String, String> entry: checkedCommit.tree().entrySet()) {
//            File blobFile = new File(_blobDirPath+File.separator+entry.getValue());
//            File WDFile = new File(entry.getKey());
//            if (WDFile.exists()) {
//                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
//                return;
//            }
//            copyFile(blobFile, WDFile);
//        }

        // set the new branchCur
        _branchStatus.setBranchCur(branchName);

        // Clear the stage area
        for (String fileName: _branchStatus.stageMap().keySet()) {
            deleteBlob(fileName);
        }
        _branchStatus.setStageMap(new TreeMap<String, String>(checkedCommit.tree()));
        _branchStatus.addList().clear();
        _branchStatus.rmList().clear();

        writeBranchStatus();
    }

    static void RMBranch(String branchName) {
        if (!readBranchStatusAndHead()) return;
        if (branchName.equals(_branchStatus.getBranchCur())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        else if (!_branchStatus.branchTable().containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        _branchStatus.removeBranchTable(branchName);
        writeBranchStatus();
    }

    static void Reset(String commitID) throws IOException {
        if (!readBranchStatusAndHead()) return;

        // Find commit via the ID
        List<Object> commitInfo = FindCommitID(commitID);
        if (commitInfo == null) {
            return;
        }
        Commit commit = (Commit) commitInfo.get(0);
        String fullID = (String) commitInfo.get(1);

        // Replace the WD
        CheckoutFiles(commit);

        // Change Head Pointer
        _branchStatus.setBranchTable(fullID);

        writeBranchStatus();
    }

    static void Merge(String branchName) throws IOException {
        if (!readBranchStatusAndHead()) return;
        // Check err conditions
        if (!_branchStatus.addList().isEmpty() || !_branchStatus.rmList().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!_branchStatus.branchTable().containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (_branchStatus.getBranchCur().equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        // Search for LCA
        LinkedHashSet<String> masterSplits = getBranchSplits(_branchStatus.getBranchTable());
        LinkedHashSet<String> branchSplits = getBranchSplits(_branchStatus.getBranchTable(branchName));
        Iterator<String> masterIter = masterSplits.iterator();
        String commitID = null;
        outerLoop:
        while (masterIter.hasNext()) {
            commitID = masterIter.next();
            for (String branchID : branchSplits) {
                if (commitID.equals(branchID)) {
                    break outerLoop;
                }
            }
        }
        // now commitID is the split point ID
        if (commitID == null) {
            throw new GitletException("No LCA Found");
        }

        if (commitID.equals(_branchStatus.getBranchTable(branchName))) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        else if (commitID.equals(_branchStatus.getBranchTable())) {
            CheckoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        Boolean isConflict = false;
        Commit LCA = Utils.readObject(new File(_logsDirPath+File.separator+commitID), Commit.class);
        Commit branch = Utils.readObject(new File(_logsDirPath+File.separator+_branchStatus.getBranchTable(branchName)), Commit.class);

        // Check if we are gonna overwrite any untracked files. We need only examine Commit branch in this case.
        // If there is a file tracked in branch that is not tracked in head, it will always be overwritten in all cases,
        // no matter it is tracked in split point commit or not.
        if (OverwriteUntrackedFiles(branch)) {
            return;
        }

        // Loop through the LCA
        for (Map.Entry<String, String> entry: LCA.tree().entrySet()) {
            String LCAValue = entry.getValue();
            String headValue = _head.tree().get(entry.getKey());
            String branchValue = branch.tree().get(entry.getKey());
//            // If the file was at the split point are both removed or removed in the current branch
//            if (headValue == null && branchValue == null) {
//                break;
//            }
//            // if the file was at the split point and removed in the given branch
//            else if (branchValue == null) {
//                if (headValue)
//            }
            // if the file has been modified in current branch
            if (!LCAValue.equals(headValue)) {
                // if the file has also been modified in given branch
                if (!LCAValue.equals(branchValue)) {
                    // If the file was at the split point are both removed
                    if (headValue == null && branchValue == null) {
                        continue;
                    }
                    // if the two file has the same version in both branches
                    else if (headValue != null && branchValue != null && headValue.equals(branchValue)) {
                        continue;
                    }
                    // else conflict
                    else {
                        isConflict = true;
                        File headFile = new File(_blobDirPath+File.separator+headValue);;
                        File branchFile = new File(_blobDirPath+File.separator+branchValue);;
                        File conflict = new File(entry.getKey());
                        Utils.writeContents(conflict, "<<<<<<< HEAD\n",
                                Utils.readContentsAsString(headFile),
                                "=======\n",
                                Utils.readContentsAsString(branchFile),
                                ">>>>>>>\n");
                        Add(conflict.getName());
                    }
                }
                // else the file is not modified in the given branch, do nothing
            }
            // special case: if the file is removed in the given branch but not changed in the current branch
            else if (branchValue == null) {
                RM(entry.getKey());
            }
            // the file has been changed in the given branch but not in the current branch
            else if (!LCAValue.equals(branchValue)) {
                CheckoutSingleFile(branch, entry.getKey());
                Add(entry.getKey());
            }
            // else the file has not been changed in both branches, do nothing
        }

        for (Map.Entry<String, String> entry: branch.tree().entrySet()) {
            String branchValue = entry.getValue();
            String headValue = _head.tree().get(entry.getKey());
            String LCAValue = LCA.tree().get(entry.getKey());
            // the file was not present at the split point
            if (LCAValue == null) {
                // the file was not present at the current branch
                if (headValue == null) {
                    CheckoutSingleFile(branch, entry.getKey());
                    Add(entry.getKey());
                }
                // conflict
                else if (!headValue.equals(branchValue)) {
                    isConflict = true;
                    File headFile = new File(_blobDirPath+File.separator+headValue);;
                    File branchFile = new File(_blobDirPath+File.separator+branchValue);;
                    File conflict = new File(entry.getKey());
                    Utils.writeContents(conflict, "<<<<<<< HEAD\n",
                            Utils.readContentsAsString(headFile),
                            "=======\n",
                            Utils.readContentsAsString(branchFile),
                            ">>>>>>>\n");
                    Add(conflict.getName());
                }
            }
        }
        Commit("Merged "+branchName+" into "+_branchStatus.getBranchCur()+".", branchName);
        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
        _branchStatus.splitPoints().add(_branchStatus.getBranchTable());
        _branchStatus.splitPoints().add(_branchStatus.getBranchTable(branchName));
        writeBranchStatus();
    }

    static LinkedHashSet<String> getBranchSplits(String headCommitID) {
        LinkedHashSet<String> branchSplits = new LinkedHashSet<String>();
        LinkedList<String> frontier = new LinkedList<String>();
        frontier.addLast(headCommitID);
        while (!frontier.isEmpty()) {
            String commitID = frontier.getFirst();
            frontier.removeFirst();
            if (_branchStatus.splitPoints().contains(commitID)) {
                branchSplits.add(commitID);
            }
            Commit commit = Utils.readObject(new File(_logsDirPath + File.separator + commitID), Commit.class);
            if (commit.getParent() != null
                    && !branchSplits.contains(commit.getParent())
                    && !frontier.contains(commit.getParent())) {
                frontier.addLast(commit.getParent());
            }
            if (commit.getSecondParent() != null
                    && !branchSplits.contains(commit.getSecondParent())
                    && !frontier.contains(commit.getSecondParent())) {
                frontier.addLast(commit.getSecondParent());
            }
        }
        return branchSplits;
    }

    static Boolean OverwriteUntrackedFiles(Commit commit) {
        // Check if the given commit will overwrite any untracked files
        for (Map.Entry<String, String> entry: commit.tree().entrySet()) {
            File WDFile = new File(entry.getKey());
            if (WDFile.exists() && !_head.tree().containsKey(entry.getKey())) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return true;
            }
        }
        return false;
    }

    static void CheckoutFiles(Commit commit) throws IOException {
        if (OverwriteUntrackedFiles(commit)) {
            return;
        }
        // delete all files that have been tracked in the current branch
        for (String fileName: _head.tree().keySet()) {
            Utils.restrictedDelete(fileName);
        }

        for (Map.Entry<String, String> entry: commit.tree().entrySet()) {
            File blobFile = new File(_blobDirPath+File.separator+entry.getValue());
            File WDFile = new File(entry.getKey());
            copyFile(blobFile, WDFile);
        }

        // Clear Stage Area
        _branchStatus.setStageMap(new TreeMap<>(commit.tree()));
        _branchStatus.addList().clear();
        _branchStatus.rmList().clear();
    }

    static List<Object> FindCommitID(String commitID) {
        Commit commit;
        String fullID = null;
        if (commitID != null) {
            File commitFile = null;
            if (commitID.length() < 40) {
                File commits = new File(_logsDirPath);
                for (File f: commits.listFiles()) {
                    if (f.getName().contains(commitID)) {
                        commitFile = f;
                        fullID = f.getName();
                        break;
                    }
                }
                if (commitFile == null) {
                    System.out.println("No commit with that id exists.");
                    return null;
                }
            }
            else {
                commitFile = new File(_logsDirPath + File.separator + commitID);
                fullID = commitID;
                if (!commitFile.exists()) {
                    System.out.println("No commit with that id exists.");
                    return null;
                }
            }
            commit = Utils.readObject(commitFile, Commit.class);
        }
        else {
            if (!readBranchStatusAndHead()) return null;
            commit = _head;
        }
        return Arrays.asList(commit, fullID);
    }

    static void printCommit(File commitFile, Commit commit) {
        System.out.print("===\n");
        System.out.printf("commit %s\n", commitFile.getName());
        System.out.printf("Date: %s\n", commit.getTimestamp());
        System.out.printf("%s\n", commit.getMessage());
        System.out.print("\n");
    }

    // Delete the blob from Staged directory.
    // It uses the stageMap in branchStatus to find the corresponding blob,
    // So be sure to call this function before deleting the entry in stageMap!
    static Boolean deleteBlob(String fileName) {
        String deletedBlobSHA = _branchStatus.stageMap().get(fileName);
        if (deletedBlobSHA == null) return false;
        File deletedBlob = new File(_stagedDirPath + File.separator + deletedBlobSHA);
        if (!deletedBlob.exists()) return false;
        return deletedBlob.delete();
    }

    static void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath());
    }

    static void writeBranchStatus() {
        File newBranchFile = new File(_branchStatusPath);
        Utils.writeObject(newBranchFile, _branchStatus);
    }

    static void writeCommit(Commit commit) {
        String fileName = Utils.sha1(Utils.serialize(commit));
        File newCommitFile = new File(_logsDirPath+File.separator+fileName);
        Utils.writeObject(newCommitFile, commit);
        _branchStatus.setBranchTable(fileName);
    }

    static Boolean readBranchStatusAndHead() {
        if (_branchStatus == null) {
            File branchFile = new File(_branchStatusPath);
            if (!branchFile.exists()) {
                System.out.println("Not in an initialized Gitlet directory. ");
                return false;
            }
            _branchStatus = Utils.readObject(branchFile, BranchStatus.class);
            File headFile = new File(_logsDirPath+File.separator+_branchStatus.getBranchTable());
            _head = Utils.readObject(headFile, Commit.class);
        }
        return true;
    }

    static String _rootDirPath = ".gitlet";

    static String _logsDirPath = _rootDirPath+File.separator+"commits";

    static String _blobDirPath = _rootDirPath+File.separator+"blobs";

    static String _branchStatusPath = _rootDirPath+File.separator+"branchFile";

    static String _stagedDirPath = _rootDirPath+File.separator+"staged";

    static BranchStatus _branchStatus;

    static Commit _head;
}

