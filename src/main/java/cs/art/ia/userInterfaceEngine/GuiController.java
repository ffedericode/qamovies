package cs.art.ia.userInterfaceEngine;

import cs.art.ia.kernel.Kernel;

import cs.art.ia.model.*;



import cs.art.ia.parserEngine.SyntaticParserEngine;
import cs.art.ia.utils.GuiResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

import java.util.ResourceBundle;

public class GuiController implements Initializable {

    @FXML
    public TableView<GuiResult> table;

    private final ObservableList<GuiResult> data = FXCollections.observableArrayList();

    @FXML
    public TextField askField;

    @FXML
    private TextArea resultArea;

    @FXML
    private Button askButton;

    @FXML
    private CheckBox synonymerFlag;

    @FXML
    private RadioButton templateFlag;

    @FXML
    private RadioButton parserFlag;

    @FXML
    private RadioButton simpleEngine;

    @FXML
    private RadioButton stemmerEngine;

    @FXML
    private RadioButton lemmatizerEngine;

    @FXML
    private CheckBox showAdvanceResult;

    @FXML
    private CheckBox showTree;

    @FXML
    private CheckBox showTreeDebug;

    @FXML
    private TextField ontologyReference;

    @FXML
    private TextField property;

    @FXML
    private TableColumn<GuiResult, String> col;

    @FXML
    private TableColumn<GuiResult, String> col1;

    @FXML
    private ProgressBar loadingBar;

    @FXML
    private Tab setting;



    private GuiResult guiViewResult;

    public void question() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Kernel.getIstance().run();
            }
        };
        thread.start();
    }

    public void getTextFromTable() {
        guiViewResult = table.getSelectionModel().getSelectedItem();
        guiViewResult.setId(table.getSelectionModel().getSelectedIndex());
        askField.setText(guiViewResult.getQuery());
    }

    public void changedTextField() {
        guiViewResult = null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Kernel.getIstance();
        Kernel.getIstance().setController(this);
        Kernel.getIstance().getKernelEngine().setTemplateParser(templateFlag.isSelected());
        Kernel.getIstance().getKernelEngine().setSyntacticParser(parserFlag.isSelected());
        Kernel.getIstance().getKernelEngine().setSynonimer(synonymerFlag.isSelected());
        Kernel.getIstance().getKernelEngine().setShowAdvanceResult(showAdvanceResult.isSelected());
        Kernel.getIstance().getKernelEngine().setOntologyReference(ontologyReference.getText());
        Kernel.getIstance().getKernelEngine().setFilterProperty(property.getText());
        SyntaticParserEngine.getInstance().switchEngine(determinatePipeline());


        String[] suggestedQuestion = {
                "Is Ridley Scott the director of A Beautiful Mind?",
                "What is the wholesaler of Whiplash?",
                "Who is the author of The Thin Red Line?",
                "Who is the photography of The Tree of Life?",
                "Who is the director of Mad Max: Fury Road?",
                "What is the budget of E.T.?",
                "Is Steven Spielberg the director of Saving Private Ryan?",
                "Who is the cinematographer of Birdman?",
                "What is the gross of Avatar?",
                "What is the runtime of The Goonies?",
                "What movies was written by James Cameron?",
                "Who is the composer of Jurassic Park?"};

        for (String s : suggestedQuestion) {
            data.add(new GuiResult(s, ""));
        }

        table.setItems(data);
        col.setCellValueFactory(new PropertyValueFactory<GuiResult, String>("query"));
        col1.setCellValueFactory(new PropertyValueFactory<GuiResult, String>("result"));

    }

    public void changePipeline() {
        SyntaticParserEngine.getInstance().switchEngine(determinatePipeline());
    }

    private String determinatePipeline() {
        if (simpleEngine.isSelected())
            return simpleEngine.getText();
        if (stemmerEngine.isSelected())
            return stemmerEngine.getText();
        if (lemmatizerEngine.isSelected())
            return lemmatizerEngine.getText();
        return null;
    }

    public void determinateParser(){
        Kernel.getIstance().getKernelEngine().setSyntacticParser(parserFlag.isSelected());
        Kernel.getIstance().getKernelEngine().setTemplateParser(templateFlag.isSelected());
    }

    public void determinateSynonimer(){
        Kernel.getIstance().getKernelEngine().setSynonimer(synonymerFlag.isSelected());
    }

    public TableView<GuiResult> getTable() {
        return table;
    }

    public ObservableList<GuiResult> getData() {
        return data;
    }

    public TextField getAskField() {
        return askField;
    }

    public TableColumn<GuiResult, String> getCol() {
        return col;
    }

    public TableColumn<GuiResult, String> getCol1() {
        return col1;
    }

    public Button getAskButton() {
        return askButton;
    }

    public GuiResult getGuiViewResult() {
        return guiViewResult;
    }

    public TextField getProperty() {
        return property;
    }

    public CheckBox getShowTree() {
        return showTree;
    }

    public CheckBox getShowTreeDebug() {
        return showTreeDebug;
    }

    public void setGuiViewResult(GuiResult guiViewResult) {
        this.guiViewResult = guiViewResult;
    }


    public Tab getSetting() {
        return setting;
    }

    public ProgressBar getLoadingBar() {
        return loadingBar;
    }


    public void appendResult(String t){
        final String text=t;
        if(showAdvanceResult.isSelected())
        Platform.runLater(new Runnable(){@Override public void run() {resultArea.appendText(text);}});
    }

    public void setResult(String t){
        final String text=t;
        Platform.runLater(new Runnable(){@Override public void run() {resultArea.setText(text);}});
    }

    public void viewTree(BinaryTree mBinaryTree) {

        if (mBinaryTree != null || mBinaryTree.getRoot() != null) {
            TextInBox root = new TextInBox(mBinaryTree.getRoot().getData(), 60, 20);
            DefaultTreeForTreeLayout<TextInBox> tree = new DefaultTreeForTreeLayout<TextInBox>(root);
            for (Node node : mBinaryTree.getRoot().getChildren()) {
                ric(tree, root, node);
            }
            // setup the tree layout configuration
            double gapBetweenLevels = 30;
            double gapBetweenNodes = 10;
            DefaultConfiguration<TextInBox> configuration = new DefaultConfiguration<TextInBox>(
                    gapBetweenLevels, gapBetweenNodes);
            // create the NodeExtentProvider for TextInBox nodes
            TextInBoxNodeExtentProvider nodeExtentProvider = new TextInBoxNodeExtentProvider();

            // create the layout
            TreeLayout<TextInBox> treeLayout = new TreeLayout<TextInBox>(tree,
                    nodeExtentProvider, configuration);
            // Create a panel that draws the nodes and edges and show the panel
            TextInBoxTreePane panel = new TextInBoxTreePane(treeLayout);
            showInDialog(panel);
        }
    }


    public void ric(DefaultTreeForTreeLayout<TextInBox> tree, TextInBox father, Node child) {
        TextInBox nodeTree;
        if (child.getChildren().size() <= 0) {
            nodeTree = new TextInBox(child.getData(), 60, 20);
        }
        nodeTree = new TextInBox(child.getData(), 60, 20);
        tree.addChild(father, nodeTree);
        for (Node node : child.getChildren()) {
            ric(tree, nodeTree, node);
        }

    }

    private static void showInDialog(JComponent panel) {

        JFrame mainFrame = new JFrame("Tree parsing");
        Container contentPane = mainFrame.getContentPane();
        contentPane.add(panel);
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainFrame.setPreferredSize(new Dimension(400, 600));
        mainFrame.pack();
        mainFrame.setVisible(true);

    }


}
