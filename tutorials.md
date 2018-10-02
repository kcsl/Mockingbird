---
layout: page
title: Tutorials
permalink: /tutorials/
---

To run L-SAP you will need to download a version of the Linux kernel, build the kernel, map the kernel code in the Atlas program analysis framework, and finally invoke the L-SAP analysis.

## Downloading the Source Code of Linux Kernel
You can download a specific Linux kernel versions from [here](https://www.kernel.org/pub/linux/kernel/v4.x/testing/). Or you can download any of the kernel versions used in the L-SAP paper: [3.17-rc1](https://www.kernel.org/pub/linux/kernel/v3.x/testing/linux-3.17-rc1.tar.gz), [3.18-rc1](https://www.kernel.org/pub/linux/kernel/v3.x/testing/linux-3.18-rc1.tar.gz), or [3.19-rc1](https://www.kernel.org/pub/linux/kernel/v3.x/testing/linux-3.19-rc1.tar.gz).

Once downloaded, extract the downloaded linux-<version>.tar.gz:

    tar -zxvf linux-<version>.tar.gz ~/linux-workspace/

Then, go to the directory `/linux-workspace/linux-<version>/`

    cd ~/linux-workspace/linux-<version>/

Then, configure the Linux kernel with your own taste of configurations. To configure the Linux kernel with the same configurations as in the L-SAP paper, run the following command:

    make allmodconfig

## Analysis Configuration
1. Because indexing the Linux kernel with Atlas requires a lot of space, we recommend that you change the parameter `-Xms` and `-Xms` in eclipse/eclipse.ini to bigger values according to your system. Note that Atlas provides a handy interface under the `Atlas` &gt; `Manage Project Settings` &gt; `Advanced` menu.

2. Run Eclipse and select `~/linux-workspace/` directory as the current Eclipse workspace.

3. Once Eclipse opens, go to `File` &gt; `Import` &gt; `Existing Code as Makefile Project`.

4. Next, browse to directory `~/linux-workspace/linux-<version>/`and choose `Linux GCC` as the Toolchain for Indexer Setting, then press `Finish`.

5. Right click on the imported Linux Kernel project and select `Properties`.

6. From `Atlas C/C++ Build` tab, click the `Enable Atlas Error Parser` button. Check (enable) the checkboxes corresponding to `Existential Indexer` and `Dataflow Indexer`.

7. From `C/C++ Build` tab, set the `Build command` to `make V=1`, From the `Behavior` tab, set the `Build (Incremental build)` field to the folder of interest to build (for example, `drivers/`). Finally, set the `Clean` command to `mrproper`.

8. From `C/C++ Buid/Settings/Error Parsers` tab, enable the `Atlas Error Parser`.

9. Build the Linux kernel by right clicking on the Linux kernel project from Eclipse and selecting `Build Project`. This process will take around 10 minutes if you have a powerful machine.

10. Next map the Linux kernel in Atlas by selecting the Linux kernel project under `Atlas` > `Manage Project Settings`, moving the project to the `Map` column, and pressing `Save &amp; Re-map`. Mapping the workspace will take some time (approximately 2-3 hours based on your machine).

11. You are now ready to run L-SAP on the Linux kernel. Please, refer to the [Tutorials](/L-SAP/tutorials) page for more info on how to run L-SAP pairing analysis.

Once Atlas has mapped the Linux kernel, you can now run the analysis scripts.

## Running L-SAP Analysis

First open the Atlas Shell to invoke L-SAP. To open the Atlas Shell from the Eclipse menu toolbar, navigate to `Atlas` &gt; `Open Atlas Shell`. 

Next run the L-SAP analysis. All the analysis logic reside in the Java class `LinuxScripts`. Before invoking the analysis scripts, you need to set the following flags/strings in code to your preferences:

- `SHOW_GRAPHS` field: if it is set to true, the script will produce the CFGs, MPGs, and EFGs for each signature in the lock/unlock pairing analysis.

- `WORKSPACE_PATH` field: to determine the path where the analysis results will be written.

If you want to invoke the spin/mutex lock/unlock pairing analysis, then write into the Atlas Shell the respective commands:

    var analysis = new LinuxScripts()
    analysis.verifySpin(true) 

For spin lock/unlock pairing analysis OR `analysis.verifyMutex(true)` for mutex lock/unlock pairing analysis. The `true`/`false` argument passed to each function correspond to whether to enable feasibility checking for the potentially-error paths if found.

The analysis results will written into `WORKSPACE_PATH` folder.