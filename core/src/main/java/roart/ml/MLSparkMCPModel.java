package roart.ml;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public  class MLSparkMCPModel  extends MLSparkModel {
    @Override
    public int getId() {
        return 1;
    }
    @Override
    public String getName() {
        return "MCP";
    }

    public Model getModel(Dataset<Row> train, int size, int outcomes) {
        //int[] layers = new int[]{size + 1, size + 1, size + 1, CATS + 1};
        //int[] layers = new int[]{size, size + 2, size + 4, CATS + 1};
        int[] layers = new int[]{size, outcomes + 1, outcomes + 1, outcomes + 1};
        //int[] layers = new int[]{size, 15, 15, outcomes + 1};
        //int[] layers = new int[]{size, size, size, outcomes + 1};
         MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier()
                .setLayers(layers)
                .setBlockSize(128)
                .setSeed(1234L)
                .setMaxIter(100);
        Model model = trainer.fit(train);
        return model;
    }
}
