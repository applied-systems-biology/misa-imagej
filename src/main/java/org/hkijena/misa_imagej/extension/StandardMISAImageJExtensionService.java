package org.hkijena.misa_imagej.extension;

import org.hkijena.misa_imagej.MISAImageJExtensionService;
import org.hkijena.misa_imagej.MISAImageJRegistryService;
import org.hkijena.misa_imagej.extension.attachmentfilters.*;
import org.hkijena.misa_imagej.extension.caches.MISAExportedAttachmentsCache;
import org.hkijena.misa_imagej.extension.caches.MISAFileCache;
import org.hkijena.misa_imagej.extension.caches.MISAOMETiffCache;
import org.hkijena.misa_imagej.extension.datasources.*;
import org.hkijena.misa_imagej.extension.outputcaches.OMETiffOutputCacheUI;
import org.hkijena.misa_imagej.extension.plotbuilder.*;
import org.hkijena.misa_imagej.extension.tableanalyzer.*;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;

@Plugin(type = MISAImageJExtensionService.class)
public class StandardMISAImageJExtensionService extends AbstractService implements MISAImageJExtensionService {

    @Override
    public void register(MISAImageJRegistryService registryService) {
        registryService.getCacheRegistry().register("misa-ome:descriptions/ome-tiff", MISAOMETiffCache.class);
        registryService.getCacheRegistry().register("misa:descriptions/file", MISAFileCache.class);
        registryService.getCacheRegistry().register("misa:descriptions/exported-attachments", MISAExportedAttachmentsCache.class);

//        registryService.getSerializableRegistry().register("misa:attachments/location", MISALocation.class);
//        registryService.getSerializableRegistry().register("misa:attachments/locatable", MISALocatable.class);
//        registryService.getSerializableRegistry().register("misa_ome:attachments/planes-location", MISAOMEPlanesLocation.class);

        registryService.getDataSourceUIRegistry().register(MISAOMETiffDataSource.class, MISAOMETiffDataSourceUI.class);
        registryService.getDataSourceUIRegistry().register(MISAFolderLinkDataSource.class, MISAFolderLinkDataSourceUI.class);
        registryService.getDataSourceUIRegistry().register(MISAPipelineNodeDataSource.class, MISAPipelineNodeDataSourceUI.class);

        registryService.getOutputCacheUIRegistry().register(MISAOMETiffCache.class, OMETiffOutputCacheUI.class);

        registryService.getAttachmentFilterUIRegistry().register(MISAAttachmentSampleFilter.class, MISAAttachmentSampleFilterUI.class,
                "Filter by sample", UIUtils.getIconFromResources("sample.png"));
        registryService.getAttachmentFilterUIRegistry().register(MISAAttachmentCacheFilter.class, MISAAttachmentCacheFilterUI.class,
                "Filter by data", UIUtils.getIconFromResources("database.png"));
        registryService.getAttachmentFilterUIRegistry().register(MISAAttachmentTypeFilter.class, MISAAttachmentTypeFilterUI.class,
                "Filter by object type", UIUtils.getIconFromResources("object.png"));
        registryService.getAttachmentFilterUIRegistry().register(MISAAttachmentRootTypeFilter.class, MISAAttachmentRootTypeFilterUI.class,
                "Filter only direct attachments", UIUtils.getIconFromResources("object.png"));
        registryService.getAttachmentFilterUIRegistry().register(MISAAttachmentSQLFilter.class, MISAAttachmentSQLFilterUI.class,
                "Filter by SQL", UIUtils.getIconFromResources("cog.png"));

        registryService.getTableAnalyzerUIOperationRegistry().register(StatisticsCountVectorOperation.class,
                null,
                "Count",
                "COUNT",
                "Counts all entries",
                UIUtils.getIconFromResources("statistics.png"));
        registryService.getTableAnalyzerUIOperationRegistry().register(StatisticsSumVectorOperation.class,
                null,
                "Sum",
                "SUM",
                "Summarizes all entries",
                UIUtils.getIconFromResources("statistics.png"));
        registryService.getTableAnalyzerUIOperationRegistry().register(StatisticsMinVectorOperation.class,
                null,
                "Minimum",
                "MIN",
                "Minimum value of entries",
                UIUtils.getIconFromResources("statistics.png"));
        registryService.getTableAnalyzerUIOperationRegistry().register(StatisticsMaxVectorOperation.class,
                null,
                "Maximum",
                "MAX",
                "Maximum value of entries",
                UIUtils.getIconFromResources("statistics.png"));
        registryService.getTableAnalyzerUIOperationRegistry().register(StatisticsMedianVectorOperation.class,
                null,
                "Median",
                "MEDIAN",
                "Median value of entries",
                UIUtils.getIconFromResources("statistics.png"));
        registryService.getTableAnalyzerUIOperationRegistry().register(StatisticsAverageVectorOperation.class,
                null,
                "Average",
                "AVG",
                "Average of entries",
                UIUtils.getIconFromResources("statistics.png"));
        registryService.getTableAnalyzerUIOperationRegistry().register(StatisticsVarianceVectorOperation.class,
                null,
                "Variance",
                "VAR",
                "Variance of entries",
                UIUtils.getIconFromResources("statistics.png"));
        registryService.getTableAnalyzerUIOperationRegistry().register(ConvertToOccurrencesVectorOperation.class,
                null,
                "Number of entries",
                "COUNT",
                "Returns the number of items",
                UIUtils.getIconFromResources("statistics.png"));

        registryService.getTableAnalyzerUIOperationRegistry().register(ConvertToNumericVectorOperation.class,
                null,
                "Convert to numbers",
                "TO_NUMBERS",
                "Ensures that all items are numbers. Non-numeric values are set to zero.",
                UIUtils.getIconFromResources("inplace-function.png"));
        registryService.getTableAnalyzerUIOperationRegistry().register(ConvertToNumericBooleanVectorOperation.class,
                null,
                "Convert to numeric boolean",
                "TO_NUMERIC_BOOLEAN",
                "Ensures that all items are numeric boolean values. Defaults to outputting zero if the value is not valid.",
                UIUtils.getIconFromResources("inplace-function.png"));
        registryService.getTableAnalyzerUIOperationRegistry().register(ConvertToOccurrencesVectorOperation.class,
                null,
                "Convert to number of occurrences",
                "TO_OCCURENCES",
                "Replaces the items by their number of occurrences within the list of items.",
                UIUtils.getIconFromResources("inplace-function.png"));
        registryService.getTableAnalyzerUIOperationRegistry().register(ConvertToNumericFactorOperation.class,
                null,
                "Convert to numeric factors",
                "TO_FACTORS",
                "Replaces each item with an ID that uniquely identifies the item.",
                UIUtils.getIconFromResources("inplace-function.png"));

        registryService.getPlotBuilderRegistry().register(StatisticalLineCategoryPlot.class,
                StatisticalCategoryPlotSettingsUI.class,
                "Statistical Line Plot",
                UIUtils.getIconFromResources("line-chart.png"));
        registryService.getPlotBuilderRegistry().register(StatisticalBarCategoryPlot.class,
                StatisticalCategoryPlotSettingsUI.class,
                "Statistical Bar Plot",
                UIUtils.getIconFromResources("bar-chart.png"));
        registryService.getPlotBuilderRegistry().register(LineCategoryPlot.class,
                CategoryPlotSettingsUI.class,
                "Line Plot",
                UIUtils.getIconFromResources("line-chart.png"));
        registryService.getPlotBuilderRegistry().register(BarCategoryPlot.class,
                CategoryPlotSettingsUI.class,
                "Bar Plot",
                UIUtils.getIconFromResources("bar-chart.png"));
        registryService.getPlotBuilderRegistry().register(StackedBarCategoryPlot.class,
                CategoryPlotSettingsUI.class,
                "Stacked Bar Plot",
                UIUtils.getIconFromResources("bar-chart.png"));
        registryService.getPlotBuilderRegistry().register(Pie2DPlot.class,
                PiePlotSettingsUI.class,
                "2D Pie Plot",
                UIUtils.getIconFromResources("pie-chart.png"));
        registryService.getPlotBuilderRegistry().register(Pie3DPlot.class,
                PiePlotSettingsUI.class,
                "3D Pie Plot",
                UIUtils.getIconFromResources("pie-chart.png"));
        registryService.getPlotBuilderRegistry().register(LineXYPlot.class,
                XYPlotSettingsUI.class,
                "XY Line Plot",
                UIUtils.getIconFromResources("line-chart.png"));
        registryService.getPlotBuilderRegistry().register(ScatterXYPlot.class,
                XYPlotSettingsUI.class,
                "XY Scatter Plot",
                UIUtils.getIconFromResources("scatter-chart.png"));
        registryService.getPlotBuilderRegistry().register(HistogramPlot.class,
                HistogramPlotSettingsUI.class,
                "Histogram Plot",
                UIUtils.getIconFromResources("bar-chart.png"));

    }
}
