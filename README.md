![Onyx Logo](https://github.com/brandmaier/onyx/blob/master/build/images/onyx-welcome.png?raw=true)

# Onyx

Onyx is a free software environment for creating and estimating structural equation models (SEM). It provides a graphical user interface that facilitates an intuitive creation of models, and a powerful back end for performing maximum likelihood estimation of parameters. Path diagrams in Onyx can be exported to OpenMx, lavaan, and Mplus to allow an easy transition to script-based SEM software. Onyx was written by Timo von Oertzen and Andreas M. Brandmaier.

# Build

If you want to build Onyx from sources, download the sources, make sure that you have a Java development kit ready (including a Java compiler) and the Apache ant build tool. Then, compile the source code using

```{bash}
ant compile
````

Then, package all compiled classes into a single jar file by issuing:

```{bash}
ant dist
```

This creates a single file `onyx.jar` in subfolder `dist`.

# Run

After you have successfully created the distributable jar file, you can run
it from the command line:

```{bash}
cd dist
java -jar onyx.jar
```

# License

Onyx is made available under the Apache 2.0 license.

Onyx uses various (unmodified) libraries, which are here redistributed under their original licenses:

- vectorgraphics2d by Erich Seifert is distributed under the LGPL
- Diff Match and Patch by Google Inc is distributed under the Apache License 2.0
- GRAL by Erich Seifert is distributed under the LGPL
- jtouchbar by Thizzer is distributed under the MIT license

Furthermore, Onyx uses the following resources:

- the mono icons by https://icons.mono.company/ distributed under the MIT
  license

