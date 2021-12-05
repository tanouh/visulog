package up.visulog.ui.controllers;

import javafx.scene.chart.Chart;
import javafx.scene.layout.HBox;
import up.visulog.analyzer.Analyzer;
import up.visulog.config.Configuration;
import up.visulog.config.PluginConfig;
import up.visulog.ui.model.Model;
import up.visulog.ui.views.View;
import up.visulog.ui.views.objects.*;
import up.visulog.ui.views.objects.chart.ChartButton;
import up.visulog.ui.views.objects.chart.ChartButtons;
import up.visulog.ui.views.scenes.VisulogScene;

import java.nio.file.FileSystems;
import java.util.HashMap;

public class Controller {

    protected View view;
    protected Model model;
    protected VisulogScene scene;
    GraphParameter graphParameter;
    MainContainer mainContainer;
    MenuRadioButton menuRadioButton;
    public Controller(View view, Model model, VisulogScene scene) {
        this.view = view;
        this.model = model;
        this.scene = scene;
    }

    public void executeAction(PluginButtons b) {// Execute le plugin lie au bouton
        if (b == null) return;
        runPlugin(b);
        this.scene.update(model.toHtml());
    }

    public void executeAction(MethodButton b) {// Execute le plugin lie au bouton
        if (b == null) return;
        executeAction((PluginButtons) b);
        if (menuRadioButton != null) {
            menuRadioButton.initMenuButtonAction(b.getPlugin());
        }

    }

    protected void runPlugin(PluginButtons b) {  //Execute le plugin Todo: amelioration de cette partie
        var PLUGINS = Model.PLUGINS;
        if (!PLUGINS.containsKey(b.getValue())) return;
        var gitPath = FileSystems.getDefault().getPath("."); //cree une variable qui contient le chemin vers ce fichier
        var plugin = new HashMap<String, PluginConfig>();
        plugin.put(b.getValue(), PLUGINS.get(b.getValue()));
        var config = new Configuration(gitPath, plugin);
        model.update(new Analyzer(config).computeResults(), b.getText());
        this.updateMainContainer();
    }

    private void updateMainContainer() {//Met ajour les graphes du mainContainer
        graphParameter.getGraphique().setVisible(true);
        graphParameter.getChildren().forEach(node -> {
            if (node instanceof ChartButton && ((ChartButton) node).isSelected()) {
                ChartButton button = (ChartButton) node;
                mainContainer.getChildren().remove(button.getChart());
                button.update(model.getCurrentPlugin());
                Chart chart = button.getChart();
                if (graphParameter.getGraphique().isSelected()) mainContainer.getChildren().add(chart);
            }
        });
    }

    public void switchWebMode(GraphParameter container) { //Passe l'application en affichage web
        container.getChildren().forEach(node -> {
            if (node instanceof ChartButtons) node.setVisible(false);
        });
        mainContainer.getChildren().removeIf(node -> node instanceof Chart);
        mainContainer.getChildren().add(mainContainer.getWeb());
    }

    public void switchGraphMode(HBox container) {   ////Passe l'application en affichage graphe
        container.getChildren().forEach(node -> {
            if (node instanceof ChartButtons) {
                node.setVisible(true);
                Chart chart = ((ChartButton) node).getChart();
                if (chart != null) mainContainer.getChildren().add(chart);
            }
        });
        mainContainer.getChildren().remove(mainContainer.getWeb());
    }

    public void applyFilter(ChartButton b) {    //Applique le filtre lie au bouton
        if (b.isSelected()) {
            b.update(model.getCurrentPlugin());
            mainContainer.getChildren().add(b.getChart());
        } else {
            b.setChart(null);
            mainContainer.getChildren().removeIf(node -> node instanceof Chart);
        }
    }

    public void setGraphParameter(GraphParameter graphParameter) {
        this.graphParameter = graphParameter;
    }

    public void setMenuRadioButton(MenuRadioButton menuRadioButton) {
        this.menuRadioButton = menuRadioButton;
    }

    public void setMainContainer(MainContainer mainContainer) {
        this.mainContainer = mainContainer;
    }
}
