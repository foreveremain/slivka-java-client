The swingjs directory contains the current transpiler (net.sf.j2s.core.jar) 
and the run-time core site files (SwingJS-site.zip)

In addition are version directories -- for example, ver/3.1.1 and ver/3.2.1

The second of these, ver/3.2.1, adds Java 8 functionality.


Local runtime fixes:
- JAL-4700: native RegExp capture indices for regex group bounds.
  Requires Chrome/Edge 90+, Firefox 88+ or Safari 15+.
- JAL-4703: correct the content-layer z-index.
- JAL-4703: expose the tab-layout helper.
- JAL-4703: correct the cubic-curve iterator method name.
- JAL-4703: start a fresh canvas path before thin strokes.
