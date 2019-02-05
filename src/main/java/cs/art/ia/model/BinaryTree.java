package cs.art.ia.model;

public class BinaryTree {
    private Node mRoot;

    public BinaryTree(Node root){
        this.mRoot = root;
    }

    public Node getRoot() {
        return mRoot;
    }

    public void setRoot(Node root) {
        this.mRoot = root;
    }
}