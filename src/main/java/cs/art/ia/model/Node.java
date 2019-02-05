package cs.art.ia.model;

import java.util.ArrayList;

public class Node {
    private Node mParent;
    private ArrayList<Node> mChildren;
    private String mData;


    public Node(String data, ArrayList<Node> children, Node parent) {
        this.mParent = parent;
        this.mChildren = children;
        this.mData = data;
    }

    public ArrayList<Node> getChildren() {
        return mChildren;
    }

    public void setChildren(ArrayList<Node> children) {
        this.mChildren = children;
    }

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        this.mData = data;
    }

    public Node getParent() {
        return mParent;
    }

    public void setParent(Node parent) {
        this.mParent = parent;
    }
}
