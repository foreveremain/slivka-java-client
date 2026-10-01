(function () {
  var C = Clazz.newClass(Clazz.newPackage('test'), 'StrokeTest');
  Clazz.newMeth(C, 'main$SA', function () {
    var result = document.getElementById('result');
    var failures = [];
    var passed = 0;
    var Graphics = Clazz.load('swingjs.JSGraphics2D');
    var Stroke = Clazz.load('java.awt.BasicStroke');
    [0.1, 0.25, 0.5, 0.75, 1, 2].forEach(function (width) {
      try {
        var canvas = document.createElement('canvas');
        canvas.width = canvas.height = 32;
        var graphics = Clazz.new_(Graphics.c$$O, [canvas]);
        graphics.setStroke$java_awt_Stroke(Clazz.new_(Stroke.c$$F, [width]));
        var ctx = canvas.getContext('2d');
        ctx.strokeStyle = 'red';
        graphics.drawLine$I$I$I$I(4, 4, 20, 4);
        var before = Array.from(ctx.getImageData(10, 4, 1, 1).data);
        ctx.strokeStyle = 'blue';
        graphics.drawLine$I$I$I$I(4, 12, 20, 12);
        var after = Array.from(ctx.getImageData(10, 4, 1, 1).data);
        var second = ctx.getImageData(10, 12, 1, 1).data;
        if (before[3] === 0 || second[2] === 0 || second[3] === 0) {
          throw 'Expected both lines to be drawn';
        }
        if (before.join(',') !== after.join(',')) {
          throw 'Drawing the second line changed the first line';
        }
        passed++;
      } catch (error) {
        failures.push('width ' + width + ': ' + error);
      }
    });
    result.textContent = passed + ' passed; ' + failures.length + ' failed\n' + failures.join('\n');
    result.dataset.status = failures.length ? 'failed' : 'passed';
  }, 1);
})();
