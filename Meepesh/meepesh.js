// Generic three.js objects are in the global namespace.
var t, renderer, scene, width, height, camera, controls, time;

onWindowResize = function() {
  width = window.innerWidth;
  height = window.innerHeight;
  camera.aspect = width / height;
  camera.updateProjectionMatrix();
  renderer.setSize(width, height);
}

// Global object for scene-specific stuff..
var MEEPESH = {
}

MEEPESH.update = function() {
  // Render the scene.
  requestAnimationFrame(MEEPESH.update);
  renderer.render(scene, camera);

  // Update controls.
  controls.update(Date.now() - time);
  time = Date.now();
}

MEEPESH.pointerLockChange = function(event) {
  if (document.pointerLockElement === MEEPESH.element ||
      document.webkitPointerLockElement === MEEPESH.element ||
      document.mozPointerLockElement === MEEPESH.element) {
    controls.enabled = true;
  } else {
    controls.enabled = false;
  }
}

MEEPESH.start = function() {
  t = THREE;
  renderer = new t.WebGLRenderer();
  width = document.body.clientWidth;
  height = document.body.clientHeight;
  renderer.setSize(width, height);
  scene = new t.Scene();
  time = Date.now();

  MEEPESH.unitSize = 20;
  MEEPESH.units = 1000;
  // Scene initially involves a floor, two lights, and a cube.
  var cube = new t.Mesh(
      new t.CubeGeometry(MEEPESH.unitSize, MEEPESH.unitSize, MEEPESH.unitSize,
                         MEEPESH.unitSize, MEEPESH.unitSize, MEEPESH.unitSize),
      new t.MeshLambertMaterial({ color: 0xFF0000 })
  );
  cube.position.set(0, MEEPESH.unitSize / 2, -2 * MEEPESH.unitSize);
  scene.add(cube);
  var geometry = new t.PlaneGeometry(
      MEEPESH.unitSize * MEEPESH.units, MEEPESH.unitSize * MEEPESH.units,
      MEEPESH.unitSize, MEEPESH.unitSize);
  // Floors generally are on the xz plane rather than the yz plane. Rotate it
  // there :).
  geometry.applyMatrix(new t.Matrix4().makeRotationX(-Math.PI / 2));
  var floor = new t.Mesh(
      geometry, new t.MeshLambertMaterial({ color: 0x00FF00 })
  );
  scene.add(floor);
  var light = new t.PointLight(0xFFFF00);
  light.position.set(
      2 * MEEPESH.unitSize, 2 * MEEPESH.unitSize, 2 * MEEPESH.unitSize);
  scene.add(light);
  var light2 = new t.PointLight(0xFFFF00);
  light2.position.set(
      -2 * MEEPESH.unitSize, 2 * MEEPESH.unitSize, -2 * MEEPESH.unitSize);
  scene.add(light2);

  // Set up controls.
  camera = new t.PerspectiveCamera(
      60,  // Field of view
      width / height,  // Aspect ratio
      1,  // Near plane
      10000  // Far plane
  );
  controls = new t.PointerLockControls(camera);
  scene.add(controls.getObject());
  var havePointerLock = 'pointerLockElement' in document ||
                        'mozPointerLockElement' in document ||
                        'webkitPointerLockElement' in document;
  if (!havePointerLock) {
    alert("No pointer lock functionality detected!");
  }
  MEEPESH.element = document.body;
  // TODO(ariw): This breaks on Firefox since we don't requestFullscreen()
  // first.
  document.addEventListener(
      'pointerlockchange', MEEPESH.pointerLockChange, false);
  document.addEventListener(
      'webkitpointerlockchange', MEEPESH.pointerLockChange, false);
  document.addEventListener(
      'mozpointerlockchange', MEEPESH.pointerLockChange, false);

  document.addEventListener(
      'pointerlockerror', function(event) {}, false);
  document.addEventListener(
      'webkitpointerlockerror', function(event) {}, false);
  document.addEventListener(
      'mozpointerlockerror', function(event) {}, false);
  MEEPESH.element.addEventListener('click', function(event) {
    MEEPESH.element.requestPointerLock =
        MEEPESH.element.requestPointerLock ||
        MEEPESH.element.webkitRequestPointerLock ||
        MEEPESH.element.mozRequestPointerLock;
    MEEPESH.element.requestPointerLock();
  }, false);


  // Get the window ready.
  document.body.appendChild(renderer.domElement);
  window.addEventListener('resize', onWindowResize, false);

  // Begin updating.
  MEEPESH.update();
}

