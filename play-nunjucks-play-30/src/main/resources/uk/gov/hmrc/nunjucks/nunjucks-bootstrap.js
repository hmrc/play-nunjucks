var nunjucks = require('nunjucks');
var callback = require('nunjucks-bootstrap');
var helpers  = require('nunjucks-helpers');

var libDirs        = process.argv.slice(2);
var fileLoader     = new nunjucks.FileSystemLoader(libDirs);
var resourceLoader = require('nunjucks-scala-loader');

var env = new nunjucks.Environment([resourceLoader, fileLoader]);

env.addGlobal("messages", helpers.messages);
env.addGlobal("csrf", helpers.csrf);

function render(view, context) {
  env.addGlobal("routes", helpers.routes);
  env.addGlobal("request", helpers.request);
  env.addGlobal("globals", helpers.globals);
  return env.render(view, context);
}

callback.setReturnValue(render);
