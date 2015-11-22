function highlight(data) {
  function highlightNode(id) {
    document.getElementById(id).children[1].removeAttribute('fill');
    document.getElementById(id).children[2].removeAttribute('fill');
    document.getElementById(id).children[1].setAttribute('class', 'highlight');
    document.getElementById(id).children[2].setAttribute('class', 'highlight');
  }

  function highlightEdge(id) {
    document.getElementById(id).children[1].setAttribute('class', 'highlight');
    document.getElementById(id).children[2].setAttribute('class', 'highlight');
    document.getElementById(id).children[2].children[0].children[0].removeAttribute('font-size');
    document.getElementById(id).children[2].children[0].children[0].setAttribute('class', 'highlight');
  }

  function restoreNode(id) {
    document.getElementById(id).children[1].removeAttribute('class');
    document.getElementById(id).children[2].removeAttribute('class');
  }

  function restoreEdge(id) {
    document.getElementById(id).children[1].removeAttribute('class');
    document.getElementById(id).children[2].removeAttribute('class');
    document.getElementById(id).children[2].children[0].children[0].removeAttribute('class');
  }

  function forData(d, nodeFn, edgeFn) {
    for (var i = 0; i < d.nodes.length; ++i) {
      nodeFn(d.nodes[i]);
    }
    for (var i = 0; i < d.edges.length; ++i) {
      edgeFn(d.edges[i]);
    }
  }

  function enterNode(event) {
    var id = event.target.id;
    forData(data[id], highlightNode, highlightEdge);
  }
  
  function leaveNode(event) {
    var id = event.target.id;
    forData(data[id], restoreNode, restoreEdge);
  }
  
  function enterEdge(event) {
    var id = event.target.id;
    forData(data[id], highlightNode, highlightEdge);
  }
  
  function leaveEdge(event) {
    var id = event.target.id;
    forData(data[id], restoreNode, restoreEdge);
  }
  
  function setupListeners(data) {
    for (var id in data) {
      forData(data[id], function (id) {
        var node = document.getElementById(id);
        node.addEventListener('mouseenter', enterNode);
        node.addEventListener('mouseleave', leaveNode);
      }, function (id) {
        var edge = document.getElementById(id);
        edge.addEventListener('mouseenter', enterEdge);
        edge.addEventListener('mouseleave', leaveEdge);
      });
    }
  }

  setupListeners(data);
}
