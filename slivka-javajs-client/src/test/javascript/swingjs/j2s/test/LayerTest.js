(function () {
  var C = Clazz.newClass(Clazz.newPackage('test'), 'LayerTest');
  Clazz.newMeth(C, 'main$SA', function () {
    var result = document.getElementById('result');
    try {
      var Pane = Clazz.load('javax.swing.JLayeredPane');
      var Panel = Clazz.load('javax.swing.JPanel');
      var pane = Clazz.new_(Pane);
      var panel = Clazz.new_(Panel);
      pane.add$java_awt_Component(panel);
      var ui = panel.getUI$();
      ui.getDOMNode$();
      pane.setLayer$java_awt_Component$I(panel, 100);
      var before = ui.domNode.style.zIndex;
      if (!before || before === 'auto') throw 'Ordinary layer did not set a z-index';
      pane.setLayer$java_awt_Component$I(panel, -30000);
      [ui.domNode, ui.outerNode].forEach(function (node) {
        if (node && node.style.zIndex && node.style.zIndex !== 'auto') {
          throw 'Content layer retains z-index ' + node.style.zIndex;
        }
      });
      pane.setLayer$java_awt_Component$I(panel, 100);
      if (ui.domNode.style.zIndex !== before) throw 'Repeated layer changes accumulate z-index';
      result.textContent = 'Layer transitions passed';
      result.dataset.status = 'passed';
    } catch (error) {
      result.textContent = 'Layer transitions failed: ' + error;
      result.dataset.status = 'failed';
    }
  }, 1);
})();
