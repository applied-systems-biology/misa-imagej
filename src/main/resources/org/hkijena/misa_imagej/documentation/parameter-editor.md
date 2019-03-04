# MISA-ImageJ parameter editor

The parameter editor tool provides a graphical user interface for a MISA++ module.
It allows you to setup input data and change algorithm, sample and runtime parameters
without the need of manually editing a parameter file or manually creating the necessary input
file structure.

This manual contains following sections:

[TOC]

## Samples

This category allows you to define the list of ![](image://icons/sample.png) samples that are
analyzed by the application. 

If you want to add samples from an existing parameter file, use the [Import parameters](#import-parameters)
functionality. If you want to quickly import samples from an existing structure of folders,
use the [Import folder](#import-folder) function. 

## Data

## Sample parameters

Sample parameters are specific to each sample. To edit sample parameters, select 
the sample using the sample selection. If there are any sample parameters, you will be able
to view and change them.

![Sample selection](image://documentation/parameter-editor/sample-selection.png)

See our guide on [how to edit parameters](#parameter-editor) to find out more about the user interface.

## Algorithm parameters

Algorithm parameters are specific to specific algorithms or groups of algorithms. 
The are *independent* from the sample that is currently analyzed.

See our guide on [how to edit parameters](#parameter-editor) to find out more about the user interface.

## Runtime

Runtime parameters are global within the whole analysis. 
Here you can change the number of threads to enable parallelization and fine-tune
application-specific settings.

See our guide on [how to edit parameters](#parameter-editor) to find out more about the user interface.

## Toolbar

### Import parameters

This function allows you to import an existing *parameter file* into the current window.

* Existing parameters will be overridden
* Existing samples will **not** be deleted
* Samples defined in the parameter file are added to the list of samples
* No data will be imported. Please use `Import folder` instead

### Import folder

The `Import folder` tool allows easy importing of parameters, samples and data from an existing 
structure of files and folders. It will ask for a root folder.

This root folder must adhere to a specific structure of files and folders:

* It can optionally contain a `parameters.json` file. It it is present, it will be loaded via the `Import parameters` function
* Subfolders represent samples. The function attempts to import any sub-folder as sample
* The structure of sample-folders is consistent with the structure visible in `Data > Input data`

For more information about the file structure, see the module documentation that can be accessed via
`Help > Module documentation`.

### Check parameters

This function will checks if the current settings are valid and if parameters or data 
is missing.

If no errors are found, the status bar will display the message `No errors found`.
If there is an issue with the current settings, the status bar will display one of the detected 
errors. 

![Example of an error within the status bar](image://documentation/parameter-editor/status-bar-error.png)

Click on the error message to display a list of all parameter checks and any additional information
why there are issues.

### Export

This function creates a read-to-use package of parameters and input data that can be copied to another computer. 

* Even if the data is already present on this computer, the data will be copied to the folder
* Parameters are validated similar to the `Check parameters` function
* Only parameters and input data are exported, not the necessary MISA++ executables

### Run

This function runs the module with the current settings.

* Copying data is avoided. Existing folders are for example only linked
* Parameters are validated similar to the `Check parameters` function
* After the analysis finished, you can open the results in the **Analysis tool**

## Parameter editor

The [sample](#sample-parameters), [algorithm-](#algorithm-parameters) and [runtime](#runtime) parameter settings share a common user interface 
that allows you to easily navigate a hierarchy of parameters.

It is separated into two sections:

* A tree representation of the parameters (left)
* The list of parameters in the currently selected subtree (right)

You can navigate the hierarchy of parameters using the tree. If you select an item, the parameter
list on the right side will update, so only the selected node and its children are displayed 
within the list.

Following the JSON standard, items can be either ![](image://icons/number.png) numbers, 
 ![](image://icons/text.png) strings,  ![](image://icons/checkbox.png) booleans or  ![](image://icons/group.png) objects.
Depending on additional information provided by the MISA++ application, the UI to edit the parameter
might change.

The parameter list offers additional filters that make it easier to navigate parameters:

 * You can deselect ![](image://icons/group.png) `Objects` to only show primitive values such as numbers or strings
 * By default, deeply nested objects are hidden to make navigation easier. Select ![](image://icons/tree.png) `Whole tree` to disable this feature
 * You can filter the list of displayed items by their name. Just type a keyword into the `Filter ...` box