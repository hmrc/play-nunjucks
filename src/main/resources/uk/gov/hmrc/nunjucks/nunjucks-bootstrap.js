var nunjucks = require('nunjucks');
var callback = require('nunjucks-bootstrap');
var helpers  = require('nunjucks-helpers');

var libDirs        = process.argv.slice(2);
var fileLoader     = new nunjucks.FileSystemLoader(libDirs);
var resourceLoader = require('nunjucks-scala-loader');

var env = new nunjucks.Environment([resourceLoader, fileLoader]);

env.addGlobal("messages", helpers.messages);
env.addGlobal("csrf", helpers.csrf);
env.addGlobal("language", helpers.language);

function render(view, context) {
  env.addGlobal("routes", helpers.routes);
  return env.render(view, context);
}

callback.setReturnValue(render);
