package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import java.util.function.Function;

public class MISAPlotSeriesGenerator<T> {
    private String name;
    private Function<Integer, T> generatorFunction;

    public MISAPlotSeriesGenerator(String name, Function<Integer, T> generatorFunction) {
        this.name = name;
        this.generatorFunction = generatorFunction;
    }

    public String getName() {
        return name;
    }

    public Function<Integer, T> getGeneratorFunction() {
        return generatorFunction;
    }
}
