# MISA-ImageJ parameter editor

The parameter editor tool provides a graphical user interface for a MISA++ module.
It allows you to setup input data and change algorithm, sample and runtime parameters
without the need of manually editing a parameter file or manually creating the necessary input
file structure.

## User interface

The user interface is separated into a toolbar and 5 categories of settings.

### Toolbar

* `Import parameters` allows you to import an existing parameter file into the current window. Please note that existing settings might be overridden. This will not import any data, although missing samples will be added.
* `Import folder` imports an existing structure of folders into the current editor. See below for a more detailed guide.
* `Check parameters` tests if the current settings are valid and if parameters or data is missing. Errors are written into the status bar.
* `Export` creates a read-to-use package of parameters and input data that can be copied to another computer. It will copy any data to the folder. Please note that this function cannot package the MISA++ module.
* `Run` allows you to run the module with the current settings on the current machine. It will attempt avoiding copying the data as much as possible (unlike the `Export` function)
* `Help` gives you access to this help and the module documentation 

### Samples

This category allows you to add, remove and rename samples.

### Data

The data editor contains the data assigned to a sample.



# Import folder

The `Import folder` tool allows easy importing of parameters, samples and data from an existing 
structure of files and folders. It will just ask for a root folder.

This root folder must adhere to a specific structure of files and folders:

* It can optionally contain a `parameters.json` file. It it is present, it will be loaded via the `Import parameters` function
* Subfolders represent samples. The function attempts to import any sub-folder as sample
* The structure of sample-folders is consistent with the structure visible in `Data > Input data`

For more information about the file structure, see the module documentation that can be accessed via
`Help > Module documentation`.