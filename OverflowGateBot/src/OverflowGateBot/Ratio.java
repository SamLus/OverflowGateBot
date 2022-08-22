package OverflowGateBot;

import java.util.HashMap;
import java.util.Set;
import arc.util.Log;

public class Ratio {

    public class RBlock {
        String name;
        HashMap<String, Float> input = new HashMap<String, Float>();
        HashMap<String, Float> output = new HashMap<String, Float>();
        Float heat = 0f;

        public RBlock(String name) {
            this.name = name;
        }

        public RBlock addInput(String item, Float amount) {
            input.put(item, amount);
            return this;
        }

        public RBlock addOutput(String item, Float amount) {
            output.put(item, amount);
            return this;
        }

        public RBlock setHeat(Float heat) {
            this.heat = heat;
            return this;
        }

    }

    RBlock BlastMixer = new RBlock("BlastMixer").addInput("Pyratite", 0.75f).addInput("SporePod", 0.75f)
            .addOutput("BlastCompound", 0.75f);
    RBlock PyratiteMixer = new RBlock("PyratiteMixer").addInput("Coal", 0.75f).addInput("Sand", 1.5f)
            .addInput("Lead", 1.5f).addOutput("Pyratite", 0.75f);
    RBlock GraphitePress = new RBlock("GraphitePress").addInput("Coal", 1.33f).addOutput("Graphite", 0.66f);
    RBlock MultiPress = new RBlock("MultiPress").addInput("Coal", 6f).addInput("Water", 6f);
    RBlock SiliconSmelter = new RBlock("SiliconSmelter").addInput("Coal", 1.5f).addInput("Sand", 3f)
            .addOutput("Silicon", 1.5f);
    RBlock SiliconCrucible = new RBlock("SiliconCrucible").addInput("Coal", 2.66f).addInput("Sand", 4f)
            .addInput("Pyratite", 0.66f).addOutput("Silicon", 5.33f);
    RBlock Klin = new RBlock("Klin").addInput("Lead", 2f).addInput("Sand", 2f).addOutput("Metaglass",
            2f);
    RBlock PlastaniumCompressor = new RBlock("PlastaniumCompressor").addInput("Titanium", 2f)
            .addInput("Oil", 15f).addOutput("Plastabium", 1f);
    RBlock PhaseWeaver = new RBlock("PhaseWeaver").addInput("Thorium", 2f).addInput("Sand", 5f)
            .addOutput("PhaseFabric", 0.5f);
    RBlock SurgeSmelter = new RBlock("SurgeSmelter").addInput("Copper", 2.4f).addInput("Lead", 3.2f)
            .addInput("Titanium", 1.6f).addInput("Silicon", 2.4f).addOutput("SurgeAlloy", 0.8f);
    RBlock Melter = new RBlock("Melter").addInput("Scrap", 6f).addOutput("Slag", 12f);
    RBlock Separator = new RBlock("Separator").addInput("Slag", 4f);
    RBlock Disassembler = new RBlock("Disassembler").addInput("Scrap", 4f).addInput("Slag", 7.1f);
    RBlock SporePress = new RBlock("SporePress").addInput("SporePod", 3f).addOutput("Oil", 18f);
    RBlock Pulverizer = new RBlock("Pulverizer").addInput("Scrap", 1.5f).addOutput("Sand", 1.5f);
    RBlock CoalCentrifuge = new RBlock("CoalCentrifuge").addInput("Oil", 6f).addOutput("Coal", 2f);
    RBlock Cultivator = new RBlock("Cultivator").addInput("Water", 18f).addOutput("SporePod", 0.66f);
    RBlock WaterExtractor = new RBlock("WaterExtractor").addOutput("Water", 6.6f);

    public class Tree {
        HashMap<String, RBlock[]> production = new HashMap<String, RBlock[]>();

        public Tree() {
            this.production.put("Coal", new RBlock[] { CoalCentrifuge });
            this.production.put("Pyratite", new RBlock[] { PyratiteMixer });
            this.production.put("Sand", new RBlock[] { Pulverizer });
            this.production.put("Silicon", new RBlock[] { SiliconSmelter, SiliconCrucible });
            this.production.put("SporePod", new RBlock[] { Cultivator });
            this.production.put("Water", new RBlock[] { WaterExtractor });
            this.production.put("Oil", new RBlock[] { SporePress });
            this.production.put("Slag", new RBlock[] { Melter });
        }
    }

    Tree tree = new Tree();

    HashMap<String, RBlock> blocks = new HashMap<String, RBlock>();

    public Ratio() {
        this.blocks.put("BlastMixer", BlastMixer);
        this.blocks.put("PyratiteMixer", PyratiteMixer);
        this.blocks.put("GraphitePress", GraphitePress);
        this.blocks.put("MultiPress", MultiPress);
        this.blocks.put("SiliconSmelter", SiliconSmelter);
        this.blocks.put("SiliconCrucible", SiliconCrucible);
        this.blocks.put("Klin", Klin);
        this.blocks.put("PlastaniumCompressor", PlastaniumCompressor);
        this.blocks.put("PhaseWeaver", PhaseWeaver);
        this.blocks.put("SurgeSmelter", SurgeSmelter);
        this.blocks.put("Melter", Melter);
        this.blocks.put("Separator", Separator);
        this.blocks.put("Disassembler", Disassembler);
        this.blocks.put("SporePress", SporePress);
        this.blocks.put("Pulverizer", Pulverizer);
        this.blocks.put("CoalCentrifuge", CoalCentrifuge);
        this.blocks.put("Cultivator", Cultivator);
        this.blocks.put("WaterExtractor", WaterExtractor);
    };

    public String getRatio(String name, int number) {
        return getRatio(name, number, new HashMap<String, String>());
    }

    public String getRatio(String name, int number, HashMap<String, String> arg) {
        String result = "";
        Ratio r = new Ratio();
        Log.info(name + ": " + number);
        result += getRatio(r, name, number, 1);
        return result;
    }

    public String getRatio(Ratio r, String name, int number, int tab) {
        String result = "";
        RBlock block = r.blocks.get(name);

        if (block == null) {
            return "Invalid block";
        }
        Set<String> items = block.input.keySet();
        if (items.size() == 0) {
            return result;
        }
        for (String item : items) {
            RBlock[] outputBlocks = r.tree.production.get(item);
            if (outputBlocks != null && outputBlocks.length > 0) {
                float amount = block.input.get(item) * number;
                RBlock outputBlock = outputBlocks[0];
                number = (int) (amount / outputBlock.output.get(item)) + 1;
                result += outputBlock.name + ": " + number + "\n";
                if (outputBlock.input.size() > 0) {

                    // getRatio(r, outputBlock.name, number, tab += 1);
                }
            }
        }
        return result;

    }

    public String Tab(int times) {
        String tab = "";
        for (int i = 0; i < times; i++) {
            tab += "\t";
        }
        return tab;
    }
}
