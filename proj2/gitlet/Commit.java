package gitlet;

import com.sun.tools.internal.ws.wsdl.document.Message;

import java.awt.color.ICC_ProfileRGB;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class Commit implements Serializable, Dumpable {

//    Fields:
    private String _message;

//    Date _timestamp;

    private String _timestamp;

    private String _parent;

    private String _secondParent;

//    private boolean _isSplit;

    private TreeMap<String, String> _tree;


//    Constructors
    Commit() {
        _message = "initial commit";
        _timestamp = DateToString(true);
        _parent = null;
        _secondParent = null;
//        _isSplit = false;
        _tree = new TreeMap<String, String>();
    }

    Commit(String message, String parent, TreeMap<String, String> tree) {
        _message = message;
        _timestamp = DateToString(false);
        _parent = parent;
        _secondParent = null;
//        _isSplit = false;
        _tree = new TreeMap<String, String>(tree);
    }

    Commit(String message, String parent, String secondParent, TreeMap<String, String> tree) {
        _message = message;
        _timestamp = DateToString(false);
        _parent = parent;
        _secondParent = secondParent;
//        _isSplit = false;
        _tree = new TreeMap<String, String>(tree);
    }

    Commit(Commit other) {
        _message = other._message;
        _timestamp = other._timestamp;
        _parent = other._parent;
        _secondParent = other._secondParent;
//        _isSplit = other._isSplit;
        _tree = new TreeMap<String, String>(other._tree);
    }

//    Commit(Commit parent, boolean inherit) {
//        this(parent);
//        _parent = ;
//        _timestamp = DateToString(false);
//    }

//    Methods

//    void Inherit(Commit parent) {
//        _message = parent._message;
//        _timestamp = DateToString(false);
//        _parent = parent._parent;
//        _tree = new TreeMap<String, String>(parent._tree);
//    }

    String DateToString(Boolean init) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy ZZZZZ", Locale.ENGLISH);
        if (init) {
            return sdf.format(new Date(0));
        }
        else {
            return sdf.format(new Date());
        }
    }

    TreeMap<String, String> tree() {
        return (TreeMap<String, String>) _tree.clone();
    }

    String getParent() {
        return _parent;
    }

    String getTimestamp() {
        return _timestamp;
    }

    String getMessage() {
        return _message;
    }

    String getSecondParent() {return _secondParent; }

    @Override
    public void dump() {
        System.out.print(_message);
        System.out.print(_timestamp);
        Iterator iter = _tree.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
            System.out.printf("Key: %s, Value: %s", entry.getKey(), entry.getValue());
        }
    }
}
