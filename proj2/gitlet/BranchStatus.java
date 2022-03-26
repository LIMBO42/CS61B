package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

public class BranchStatus implements Serializable, Dumpable {

    BranchStatus() {
        _branchCur = "master";
        _branchTable = new TreeMap<String, String>();
        _stageMap = new TreeMap<String, String>();
        _addList = new ArrayList<String>();
        _rmList = new ArrayList<String>();
        _splitPoints = new HashSet<String>();
        /* Test */
//        _addList.add("This");
//        _addList.add("is");
//        _rmList.add("a");
//        _rmList.add("Test");
    }

    public void setBranchTable(String key, String value) {
        _branchTable.put(key, value);
    }

    public void setBranchTable(String value) {_branchTable.put(_branchCur, value); }

    public String getBranchTable() {
        return _branchTable.get(_branchCur);
    }

    public String getBranchTable(String key) {
        return _branchTable.get(key);
    }

    public Boolean containsKeyBranchTable(String key) { return _branchTable.containsKey(key); }

    public void removeBranchTable(String key) { _branchTable.remove(key); }

    public TreeMap<String, String> branchTable() {
        return (TreeMap<String, String>) _branchTable.clone();
    }

    public TreeMap<String, String> stageMap() {
        return _stageMap;
    }

    public ArrayList<String> addList() {
        return _addList;
    }

    public ArrayList<String> rmList() {
        return _rmList;
    }

    public String getBranchCur() {
        return _branchCur;
    }

    public void setBranchCur(String branchName) {
        _branchCur = branchName;
    }

    public void setStageMap(TreeMap<String, String> newMap) {
        _stageMap = newMap;
    }

    public HashSet<String> splitPoints() { return _splitPoints; }

    @Override
    public void dump() {
        //TODO
    }

    private String _branchCur;

    private TreeMap<String, String> _branchTable;

    private ArrayList<String> _addList;

    private ArrayList<String> _rmList;

    private TreeMap<String, String> _stageMap;

    private HashSet<String> _splitPoints;
}
