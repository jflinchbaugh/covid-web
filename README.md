# COVID Web

A React/Reagent frontend to display data from JSON files produced by the covid-data project.

### Development mode

The app was initialized from the `reagent-frontend` template:
```
lein new reagent-frontend covid-web
```

To start the Figwheel compiler, navigate to the project folder and run the following command in the terminal:

```
lein figwheel
```

Figwheel will automatically push cljs changes to the browser.
Once Figwheel starts up, you should be able to open the `public/index.html` page in the browser.

### REPL

The project is setup to start nREPL on port `7002` once Figwheel starts.
Once you connect to the nREPL, run `(cljs)` to switch to the ClojureScript REPL.

### Emacs CIDER

To work on the project in emacs, start up emacs, jack into clj+cljs, choose `figwheel` when prompted,
and it'll start up the server from emacs. Navigate to the http://loalhost:3449/ to browse the app.

### Building for Production

```
lein clean
lein package
```

### Deploying Production Artifacts

Copy contents of `public` directory to a static site.
