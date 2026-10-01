(function () {
  var C = Clazz.newClass(Clazz.newPackage('test'), 'RegexTest');
  Clazz.newMeth(C, 'main$SA', function () {
    var result = document.getElementById('result');
    var failures = [];
    var passed = 0;
    function test(name, run) {
      try {
        run();
        passed++;
      } catch (error) {
        failures.push(name + ': ' + error);
      }
    }
    function equal(actual, expected) {
      if (actual !== expected) {
        throw JSON.stringify(actual) + ' != ' + JSON.stringify(expected);
      }
    }
    var Pattern = Clazz.load('java.util.regex.Pattern');
    function matcher(regex, input) {
      return Pattern.compile$S(regex).matcher$CharSequence(input);
    }
    function groups(regex, input, expected) {
      var m = matcher(regex, input);
      equal(m.find$(), true);
      equal(m.groupCount$(), expected.length - 1);
      expected.forEach(function (group, i) {
        equal(m.group$I(i), group[0]);
        equal(m.start$I(i), group[1]);
        equal(m.end$I(i), group[2]);
      });
      return m;
    }
    [
      ["a='", true], ["a='hello", true], ["a=''", false],
      ["a='hello'", false], ['', false], ["a='\n", true]
    ].forEach(function (entry) {
      test('JAL-4700 matches ' + JSON.stringify(entry[0]), function () {
        equal(!!matcher(".*='[^']*(?!')", entry[0]).matches$(), entry[1]);
      });
    });
    test('JAL-4700 find rejects a closing quote', function () {
      equal(matcher(".*='[^']*(?!')", "a=''").find$(), false);
    });
    test('negative lookahead rejects its content', function () {
      equal(matcher('a(?!b)', 'ab').find$(), false);
    });
    test('positive lookahead requires its content', function () {
      equal(matcher('a(?=b)', 'ac').find$(), false);
    });
    test('lookahead has no capture of its own', function () {
      groups('a(?=b)', 'xab', [['a', 1, 2]]);
      groups('a(?!b)', 'xac', [['a', 1, 2]]);
    });
    test('captures after positive lookahead', function () {
      groups('a(?=b)(b)', 'xab', [['ab', 1, 3], ['b', 2, 3]]);
    });
    test('captures around negative lookahead', function () {
      groups('(a)(?!b)(c)', 'xac', [['ac', 1, 3], ['a', 1, 2], ['c', 2, 3]]);
    });
    test('captures inside and after lookahead', function () {
      groups('(?=(a))(a)', 'xa', [['a', 1, 2], ['a', 1, 2], ['a', 1, 2]]);
      groups('(?=a(b))(ab)', 'xab', [['ab', 1, 3], ['b', 2, 3], ['ab', 1, 3]]);
    });
    test('noncapturing prefixes inside lookahead', function () {
      groups('(?=(?:a)(b))(ab)', 'xab', [['ab', 1, 3], ['b', 2, 3], ['ab', 1, 3]]);
      groups('(?=(?:a(?:b))(c))(abc)', 'xabc', [['abc', 1, 4], ['c', 3, 4], ['abc', 1, 4]]);
    });
    test('quantified noncapturing prefixes', function () {
      groups('x(?:ab)+(c)', 'xababc', [['xababc', 0, 6], ['c', 5, 6]]);
      groups('(?=(?:ab)+(c))(ababc)', 'xababc', [['ababc', 1, 6], ['c', 5, 6], ['ababc', 1, 6]]);
    });
    test('noncapturing prefix preserves backreferences', function () {
      groups('(?:a)(b)\\1', 'abb', [['abb', 0, 3], ['b', 1, 2]]);
      groups('x(a)\\1', 'xaa', [['xaa', 0, 3], ['a', 1, 2]]);
    });
    test('alternation preserves capture numbering', function () {
      groups('(a)|(b)', 'b', [['b', 0, 1], [null, -1, -1], ['b', 0, 1]]);
    });
    test('repeated captures use the last iteration', function () {
      groups('(a)+(b)', 'aab', [['aab', 0, 3], ['a', 1, 2], ['b', 2, 3]]);
      groups('(a){2}', 'aa', [['aa', 0, 2], ['a', 1, 2]]);
    });
    test('match result retains capture positions after the matcher advances', function () {
      var m = groups('(a)', 'aa', [['a', 0, 1], ['a', 0, 1]]);
      var snapshot = m.toMatchResult$();
      equal(m.find$(), true);
      equal(m.start$I(1), 1);
      equal(snapshot.group$I(1), 'a');
      equal(snapshot.start$I(1), 0);
      equal(snapshot.end$I(1), 1);
    });
    test('capture positions remain absolute within a region', function () {
      var m = matcher('(b)', 'xbxb').region$I$I(1, 3);
      equal(m.find$(), true);
      equal(m.start$I(1), 1);
      equal(m.end$I(1), 2);
      equal(m.find$(), false);
    });
    test('unmatched captures', function () {
      groups('(?!(a))(b)', 'xb', [['b', 1, 2], [null, -1, -1], ['b', 1, 2]]);
      groups('(a)?b', 'b', [['b', 0, 1], [null, -1, -1]]);
    });
    test('nested lookaheads', function () {
      groups('(?=(a(?=b)))(ab)', 'xab', [['ab', 1, 3], ['a', 1, 2], ['ab', 1, 3]]);
    });
    test('named captures', function () {
      var m = groups('(?=(?<ahead>a))(?<after>a)', 'xa',
        [['a', 1, 2], ['a', 1, 2], ['a', 1, 2]]);
      equal(m.group$S('ahead'), 'a');
      equal(m.start$S('after'), 1);
      equal(m.end$S('after'), 2);
    });
    test('escaped and class parentheses', function () {
      groups('(?=\\()([(])', 'x(', [['(', 1, 2], ['(', 1, 2]]);
      groups('(?=[)])([)])', 'x)', [[')', 1, 2], [')', 1, 2]]);
    });
    test('ordinary capture positions', function () {
      groups('(a)(b)', 'xab', [['ab', 1, 3], ['a', 1, 2], ['b', 2, 3]]);
      var named = groups('(?<first>a)(?<second>b)', 'xab', [['ab', 1, 3], ['a', 1, 2], ['b', 2, 3]]);
      equal(named.group$S('second'), 'b');
    });
    test('empty lookahead and expressions without captures', function () {
      groups('(?=)', 'a', [['', 0, 0]]);
      groups('a', 'a', [['a', 0, 1]]);
      groups('', 'a', [['', 0, 0]]);
    });
    test('successive finds reset lookahead positions', function () {
      var m = matcher('(?=(a))(a)', 'aa');
      equal(m.find$(), true);
      equal(m.start$I(2), 0);
      equal(m.find$(), true);
      equal(m.start$I(2), 1);
      equal(m.find$(), false);
    });
    result.textContent = passed + ' passed; ' + failures.length + ' failed\n' + failures.join('\n');
    result.dataset.status = failures.length ? 'failed' : 'passed';
  }, 1);
})();
