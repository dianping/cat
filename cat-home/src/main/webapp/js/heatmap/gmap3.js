/*
 *  GMAP3 Plugin for JQuery 
 *  Version   : 4.1
 *  Date      : 2011-11-18
 *  Licence   : GPL v3 : http://www.gnu.org/licenses/gpl.html  
 *  Author    : DEMONTE Jean-Baptiste
 *  Contact   : jbdemonte@gmail.com
 *  Web site  : http://gmap3.net
 *   
 *  Copyright (c) 2010-2011 Jean-Baptiste DEMONTE
 *  All rights reserved.
 *   
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above 
 *     copyright notice, this list of conditions and the following 
 *     disclaimer in the documentation and/or other materials provided 
 *     with the distribution.
 *   - Neither the name of the author nor the names of its contributors 
 *     may be used to endorse or promote products derived from this 
 *     software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
 
 (function ($) {
  
  /***************************************************************************/
  /*                                STACK                                    */
  /***************************************************************************/
  function Stack (){
    var st = [];
    this.empty = function (){
      for(var i = 0; i < st.length; i++){
        if (st[i]){
          return false
        }
      }
      return true;
    }
    this.add = function(v){
      st.push(v);
    }
    this.addNext = function ( v){
      var t=[], i, k = 0;
      for(i = 0; i < st.length; i++){
        if (!st[i]){
          continue;
        }
        if (k == 1) {
          t.push(v);
        }
        t.push(st[i]);
        k++;
      }
      if (k < 2) {
        t.push(v);
      }
      st = t;
    }
    this.get = function (){
      for(var i = 0; i < st.length; i++){
        if (st[i]) {
          return st[i];
        }
      }
      return false;
    }
    this.ack = function (){
      for(var i = 0; i < st.length; i++){                     
        if (st[i]) {
          delete st[i];
          break;
        }
      }
      if (this.empty()){
        st = [];
      }
    }
  }
  
  /***************************************************************************/
  /*                                STORE                                    */
  /***************************************************************************/
  function Store(){
    var store = {};
    
    /**
     * add a mixed to the store
     **/
    this.add = function(name, obj, todo){
      name = name.toLowerCase();
      if (!store[name]){
        store[name] = [];
      }
      store[name].push({obj:obj, tag:ival(todo, 'tag')});
      return name + '-' + (store[name].length-1);
    }
    
    /**
     * return a stored mixed
     **/
    this.get = function(name, last, tag){
      var i, idx, add;
      name = name.toLowerCase();
      if (!store[name] || !store[name].length){
        return null;
      }
      idx = last ? store[name].length : -1;
      add = last ? -1 : 1;
      for(i=0; i<store[name].length; i++){
        idx += add;
        if (store[name][idx]){
          if (tag !== undefined) {
            if ( (store[name][idx].tag === undefined) || ($.inArray(store[name][idx].tag, tag) < 0) ){
              continue;
            }
          }
          return store[name][idx].obj;
        }
      }
      return null;
    }
    
    /**
     * return all stored mixed
     **/
    this.all = function(name, tag){
      var i, result = [];
      name = name.toLowerCase();
      if (!store[name] || !store[name].length){
        return result;
      }
      for(i=0; i<store[name].length; i++){
        if (!store[name][i]){
          continue;
        }
        if ( (tag !== undefined) && ( (store[name][i].tag === undefined) || ($.inArray(store[name][i].tag, tag) < 0) ) ){
          continue;
        }
        result.push(store[name][i].obj);
      }
      return result;
    }
    
    /**
     * return all storation groups
     **/
    this.names = function(){
      var name, result = [];
      for(name in store){
        result.push(name);
      }
      return result;
    }
    
    /**
     * return an object from its reference
     **/
    this.refToObj = function(ref){
      ref = ref.split('-'); // name - idx
      if ((ref.length == 2) && store[ref[0]] && store[ref[0]][ref[1]]){
        return store[ref[0]][ref[1]].obj;
      }
      return null;
    }
    
    /**
     * remove one object from the store
     **/
    this.rm = function(name, tag, pop){
      var idx, i, tmp;
      name = name.toLowerCase();
      if (!store[name]) {
        return false;
      }
      if (tag !== undefined){
        if (pop){
          for(idx = store[name].length - 1; idx >= 0; idx--){
            if ( (store[name][idx] !== undefined) && (store[name][idx].tag !== undefined) && ($.inArray(store[name][idx].tag, tag) >= 0) ){
              break;
            }
          }
        } else {
          for(idx = 0; idx < store[name].length; idx++){
            if ( (store[name][idx] !== undefined) && (store[name][idx].tag !== undefined) && ($.inArray(store[name][idx].tag, tag) >= 0) ){
              break;
            }
          }
        }
      } else {
        idx = pop ? store[name].length - 1 : 0;
      }
      if ( !(idx in store[name]) ) {
        return false;
      }
      // Google maps element
      if (typeof(store[name][idx].obj.setMap) === 'function') {
        store[name][idx].obj.setMap(null);
      }
      // jQuery
      if (typeof(store[name][idx].obj.remove) === 'function') {
        store[name][idx].obj.remove();
      }
      // internal (cluster)
      if (typeof(store[name][idx].obj.free) === 'function') {
        store[name][idx].obj.free();
      }
      delete store[name][idx].obj;
      if (tag !== undefined){
        tmp = [];
        for(i=0; i<store[name].length; i++){
          if (i !== idx){
            tmp.push(store[name][i]);
          }
        }
        store[name] = tmp;
      } else {
        if (pop) {
          store[name].pop();
        } else {
          store[name].shift();
        }
      }
      return true;
    }
    
    /**
     * remove objects from the store
     **/
    this.clear = function(list, last, first, tag){
      var k, i, name;
      if (!list || !list.length){
        list = [];
        for(k in store){
          list.push(k);
        }
      } else {
        list = array(list);
      }
      for(i=0; i<list.length; i++){
        if (list[i]){
          name = list[i].toLowerCase();
          if (!store[name]){
            continue;
          }
          if (last){
            this.rm(name, tag, true);
          } else if (first){
            this.rm(name, tag, false);
          } else {
            // all
            while (this.rm(name, tag, false));
          }
        }
      }
    }
  }
  
  /***************************************************************************/
  /*                              CLUSTERER                                  */
  /***************************************************************************/

  function Clusterer(){
    var markers = [], events=[], stored=[], latest=[], redrawing = false, redraw;
    
    this.events = function(){
      for(var i=0; i<arguments.length; i++){
        events.push(arguments[i]);
      }
    }
    
    this.startRedraw = function(){
      if (!redrawing){
        redrawing = true;
        return true;
      }
      return false;
    }
    
    this.endRedraw = function(){
      redrawing = false;
    }
    
    this.redraw = function(){
      var i, args = [], that = this; 
      for(i=0; i<arguments.length; i++){
        args.push(arguments[i]);
      }
      if (this.startRedraw){
        redraw.apply(that, args);
        this.endRedraw();
      } else {
        setTimeout(function(){
            that.redraw.apply(that, args);
          },
          50
        );
      }
    };
    
    this.setRedraw = function(fnc){
      redraw  = fnc;
    }
    
    this.store = function(data, obj, shadow){
      stored.push({data:data, obj:obj, shadow:shadow});
    }
    
    this.free = function(){
      for(var i = 0; i < events.length; i++){
        google.maps.event.removeListener(events[i]);
      }
      events=[];
      this.freeAll();
    }
    
    this.freeIndex = function(i){
      if (typeof(stored[i].obj.setMap) === 'function') {
        stored[i].obj.setMap(null);
      }
      if (typeof(stored[i].obj.remove) === 'function') {
        stored[i].obj.remove();
      }
      if (stored[i].shadow){ // only overlays has shadow
        if (typeof(stored[i].shadow.remove) === 'function') {
          stored[i].obj.remove();
        }
        if (typeof(stored[i].shadow.setMap) === 'function') {
          stored[i].shadow.setMap(null);
        }
        delete stored[i].shadow;
      }
      delete stored[i].obj;
      delete stored[i].data;
      delete stored[i];
    }
    
    this.freeAll = function(){
      var i;
      for(i = 0; i < stored.length; i++){
        if (stored[i]) {
          this.freeIndex(i);
        }
      }
      stored = [];
    }
    
    this.freeDiff = function(clusters){
      var i, j, same = {}, idx = [];
      for(i=0; i<clusters.length; i++){
        idx.push( clusters[i].idx.join('-') );
      }
      for(i = 0; i < stored.length; i++){
        if (!stored[i]) {
          continue;
        }
        j = $.inArray(stored[i].data.idx.join('-'), idx);
        if (j >= 0){
          same[j] = true;
        } else {
          this.freeIndex(i);
        }
      }
      return same;
    }
    
    this.add = function(latLng, marker){
      markers.push({latLng:latLng, marker:marker});
    }
    
    this.get = function(i){
      return markers[i];
    }
    
    this.clusters = function(map, radius, maxZoom, force){
      var proj = map.getProjection(),
          nwP = proj.fromLatLngToPoint(
            new google.maps.LatLng(
                map.getBounds().getNorthEast().lat(),
                map.getBounds().getSouthWest().lng()
            )
          ),
          i, j, j2, p, x, y, k, k2, 
          z = map.getZoom(),
          pos = {}, 
          saved = {},
          unik = {},
          clusters = [],
          cluster,
          chk,
          lat, lng, keys, cnt,
          bounds = map.getBounds(),
          noClusters = maxZoom && (maxZoom <= map.getZoom()),
          chkContain = map.getZoom() > 2;
      
      cnt = 0;
      keys = {};
      for(i = 0; i < markers.length; i++){
        if (chkContain && !bounds.contains(markers[i].latLng)){
          continue;
        }
        p = proj.fromLatLngToPoint(markers[i].latLng);
        pos[i] = [
          Math.floor((p.x - nwP.x) * Math.pow(2, z)),
          Math.floor((p.y - nwP.y) * Math.pow(2, z))
        ];
        keys[i] = true;
        cnt++;
      }
      // check if visible markers have changed 
      if (!force && !noClusters){
        for(k = 0; k < latest.length; k++){
          if( k in keys ){
            cnt--;
          } else {
            break;
          }
        }
        if (!cnt){
          return false; // no change
        }
      }
      
      // save current keys to check later if an update has been done 
      latest = keys;
      
      keys = [];
      for(i in pos){
        x = pos[i][0];
        y = pos[i][1];
        if ( !(x in saved) ){
          saved[x] = {};
        }
        if (!( y in saved[x]) ) {
          saved[x][y] = i;
          unik[i] = {};
          keys.push(i);
        }
        unik[ saved[x][y] ][i] = true;
      }
      radius = Math.pow(radius, 2);
      delete(saved);
      
      k = 0;
      while(1){
        while((k <keys.length) && !(keys[k] in unik)){
          k++;
        }
        if (k == keys.length){
          break;
        }
        i = keys[k];
        lat = pos[i][0];
        lng = pos[i][1];
        saved = null;
        
        
        if (noClusters){
          saved = {lat:lat, lng:lng, idx:[i]};
        } else {
          do{
            cluster = {lat:0, lng:0, idx:[]};
            for(k2 = k; k2<keys.length; k2++){
              if (!(keys[k2] in unik)){
                continue;
              }
              j = keys[k2];
              if ( Math.pow(lat - pos[j][0], 2) + Math.pow(lng-pos[j][1], 2) <= radius ){
                for(j2 in unik[j]){
                  cluster.lat += markers[j2].latLng.lat();
                  cluster.lng += markers[j2].latLng.lng();
                  cluster.idx.push(j2);
                }
              }
            }
            cluster.lat /= cluster.idx.length;
            cluster.lng /= cluster.idx.length;
            if (!saved){
              chk = cluster.idx.length > 1;
              saved = cluster;
            } else {
              chk = cluster.idx.length > saved.idx.length;
              if (chk){
                saved = cluster;
              }
            }
            if (chk){
              p = proj.fromLatLngToPoint( new google.maps.LatLng(saved.lat, saved.lng) );
              lat = Math.floor((p.x - nwP.x) * Math.pow(2, z));
              lng = Math.floor((p.y - nwP.y) * Math.pow(2, z));
            }
          } while(chk);
        }
         
        for(k2 = 0; k2 < saved.idx.length; k2++){
          if (saved.idx[k2] in unik){
            delete(unik[saved.idx[k2]]);
          }
        }
        clusters.push(saved);
      }
      return clusters;
    }
    
    this.getBounds = function(){
      var i, bounds = new google.maps.LatLngBounds();
      for(i=0; i<markers.length; i++){
        bounds.extend(markers[i].latLng);
      }
      return bounds;
    }
  }

  /***************************************************************************/
  /*                           GMAP3 GLOBALS                                 */
  /***************************************************************************/
  
  var _default = {
      verbose:false,
      queryLimit:{
        attempt:5,
        delay:250, // setTimeout(..., delay + random);
        random:250
      },
      init:{
        mapTypeId : google.maps.MapTypeId.ROADMAP,
        center:[46.578498,2.457275],
        zoom: 2
      },
      classes:{
        Map               : google.maps.Map,
        Marker            : google.maps.Marker,
        InfoWindow        : google.maps.InfoWindow,
        Circle            : google.maps.Circle,
        Rectangle         : google.maps.Rectangle,
        OverlayView       : google.maps.OverlayView,
        StreetViewPanorama: google.maps.StreetViewPanorama,
        KmlLayer          : google.maps.KmlLayer,
        TrafficLayer      : google.maps.TrafficLayer,
        BicyclingLayer    : google.maps.BicyclingLayer,
        GroundOverlay     : google.maps.GroundOverlay,
        StyledMapType     : google.maps.StyledMapType
      }
    },
    _properties = ['events','onces','options','apply', 'callback', 'data', 'tag'],
    _noInit = ['init', 'geolatlng', 'getlatlng', 'getroute', 'getelevation', 'getdistance', 'addstyledmap', 'setdefault', 'destroy'],
    _directs = ['get'],
    geocoder = directionsService = elevationService = maxZoomService = distanceMatrixService = null;
    
  function setDefault(values){
    for(var k in values){
      if (typeof(_default[k]) === 'object'){
        _default[k] = $.extend({}, _default[k], values[k]);
      } else {
        _default[k] = values[k];
      }
    }
  }
  
  function autoInit(iname){
    if (!iname){
      return true;
    }
    for(var i = 0; i < _noInit.length; i++){
      if (_noInit[i] === iname) {
        return false;
      }
    }
    return true;
  }
  
    
  /**
   * return true if action has to be executed directly
   **/
  function isDirect (todo){
    var action = ival(todo, 'action');
    for(var i = 0; i < _directs.length; i++){
      if (_directs[i] === action) {
        return true;
      }
    }
    return false;
  }
        
  //-----------------------------------------------------------------------//
  // Objects tools
  //-----------------------------------------------------------------------//
  
  /**
   * return the real key by an insensitive seach
   **/
  function ikey (object, key){
    if (key.toLowerCase){
      key = key.toLowerCase();
      for(var k in object){
        if (k.toLowerCase && (k.toLowerCase() == key)) {
          return k;
        }
      }
    }
    return false;
  }
  
  /**
   * return the value of real key by an insensitive seach
   **/
  function ival (object, key, def){
    var k = ikey(object, key);
    return k ? object[k] : def;
  }
  
  /**
   * return true if at least one key is set in object
   * nb: keys in lowercase
   **/
  function hasKey (object, keys){
    var n, k;
    if (!object || !keys) {
      return false;
    }
    keys = array(keys);
    for(n in object){
      if (n.toLowerCase){
        n = n.toLowerCase();
        for(k in keys){
          if (n == keys[k]) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  /**
   * return a standard object
   * nb: include in lowercase
   **/
  function extractObject (todo, include, result/* = {} */){
    if (hasKey(todo, _properties) || hasKey(todo, include)){ // #1 classical object definition
      var i, k;
      // get defined properties values from todo
      for(i=0; i<_properties.length; i++){
        k = ikey(todo, _properties[i]);
        result[ _properties[i] ] = k ? todo[k] : {};
      }
      if (include && include.length){
        for(i=0; i<include.length; i++){
          if(k = ikey(todo, include[i])){
            result[ include[i] ] = todo[k];
          }
        }
      }
      return result;
    } else { // #2 simplified object (all excepted "action" are options properties)
      result.options= {};
      for(k in todo){
        if (k !== 'action'){
          result.options[k] = todo[k];
        }
      }
      return result;
    }
  }
  
  /**
   * identify object from object list or parameters list : [ objectName:{data} ] or [ otherObject:{}, ] or [ object properties ]
   * nb: include, exclude in lowercase
   **/
  function getObject(name, todo, include, exclude){
    var iname = ikey(todo, name),
        i, result = {}, keys=['map'];
    // include callback from high level
    result['callback'] = ival(todo, 'callback');
    include = array(include);
    exclude = array(exclude);
    if (iname) {
      return extractObject(todo[iname], include, result);
    }
    if (exclude && exclude.length){
      for(i=0; i<exclude.length; i++) {
        keys.push(exclude[i]);
      }
    }
    if (!hasKey(todo, keys)){
      result = extractObject(todo, include, result);
    }
    // initialize missing properties
    for(i=0; i<_properties.length; i++){
      if (_properties[i] in result){
        continue;
      }
      result[ _properties[i] ] = {};
    }
    return result;
  }
  
  //-----------------------------------------------------------------------//
  // Service tools
  //-----------------------------------------------------------------------//
    
  function getGeocoder(){
    if (!geocoder) {
      geocoder = new google.maps.Geocoder();
    }
    return geocoder;
  }
  
  function getDirectionsService(){
    if (!directionsService) {
      directionsService = new google.maps.DirectionsService();
    }
    return directionsService;
  }
  
  function getElevationService(){
    if (!elevationService) {
      elevationService = new google.maps.ElevationService();
    }
    return elevationService;
  }
  
  function getMaxZoomService(){
    if (!maxZoomService) {
      maxZoomService = new google.maps.MaxZoomService();
    }
    return maxZoomService;
  }
  
  function getDistanceMatrixService(){
    if (!distanceMatrixService) {
      distanceMatrixService = new google.maps.DistanceMatrixService();
    }
    return distanceMatrixService;
  }
    
  //-----------------------------------------------------------------------//
  // Unit tools
  //-----------------------------------------------------------------------//
  
  /**
   * return true if mixed is usable as number
   **/
  function numeric(mixed){
    return (typeof(mixed) === 'number' || typeof(mixed) === 'string') && mixed !== '' && !isNaN(mixed);
  }
  
    /**
   * convert data to array
   **/
  function array(mixed){
    var k, a = [];
    if (mixed !== undefined){
      if (typeof(mixed) === 'object'){
        if (typeof(mixed.length) === 'number') {
          a = mixed;
        } else {
          for(k in mixed) {
            a.push(mixed[k]);
          }
        }
      } else{ 
        a.push(mixed);
      }
    }
    return a;
  }
  
  /**
   * convert mixed [ lat, lng ] objet to google.maps.LatLng
   **/
  function toLatLng (mixed, emptyReturnMixed, noFlat){
    var empty = emptyReturnMixed ? mixed : null;
    if (!mixed || (typeof(mixed) === 'string')){
      return empty;
    }
    // defined latLng
    if (mixed.latLng) {
      return toLatLng(mixed.latLng);
    }
    // google.maps.LatLng object
    if (typeof(mixed.lat) === 'function') {
      return mixed;
    } 
    // {lat:X, lng:Y} object
    else if ( numeric(mixed.lat) ) {
      return new google.maps.LatLng(mixed.lat, mixed.lng);
    }
    // [X, Y] object 
    else if ( !noFlat && mixed.length){ // and "no flat" object allowed
      if ( !numeric(mixed[0]) || !numeric(mixed[1]) ) {
        return empty;
      }
      return new google.maps.LatLng(mixed[0], mixed[1]);
    }
    return empty;
  }
  
  /**
   * convert mixed [ sw, ne ] object by google.maps.LatLngBounds
   **/
  function toLatLngBounds(mixed, flatAllowed, emptyReturnMixed){
    var ne, sw, empty;
    if (!mixed) {
      return null;
    }
    empty = emptyReturnMixed ? mixed : null;
    if (typeof(mixed.getCenter) === 'function') {
      return mixed;
    }
    if (mixed.length){
      if (mixed.length == 2){
        ne = toLatLng(mixed[0]);
        sw = toLatLng(mixed[1]);
      } else if (mixed.length == 4){
        ne = toLatLng([mixed[0], mixed[1]]);
        sw = toLatLng([mixed[2], mixed[3]]);
      }
    } else {
      if ( ('ne' in mixed) && ('sw' in mixed) ){
        ne = toLatLng(mixed.ne);
        sw = toLatLng(mixed.sw);
      } else if ( ('n' in mixed) && ('e' in mixed) && ('s' in mixed) && ('w' in mixed) ){
        ne = toLatLng([mixed.n, mixed.e]);
        sw = toLatLng([mixed.s, mixed.w]);
      }
    }
    if (ne && sw){
      return new google.maps.LatLngBounds(sw, ne);
    }
    return empty;
  }

  /***************************************************************************/
  /*                                GMAP3                                    */
  /***************************************************************************/
  
  function Gmap3($this){
  
    var stack = new Stack(),
        store = new Store(),
        map = null,
        styles = {},
        running = false;
    
    //-----------------------------------------------------------------------//
    // Stack tools
    //-----------------------------------------------------------------------//
        
    /**
     * store actions to execute in a stack manager
     **/
    this._plan = function(list){
      for(var k = 0; k < list.length; k++) {
        stack.add(list[k]);
      }
      this._run();
    }
     
    /**
     * store one action to execute in a stack manager after the current
     **/
    this._planNext = function(todo){
      stack.addNext(todo);
    }
    
    /**
     * execute action directly
     **/
    this._direct = function(todo){
      var action = ival(todo, 'action');
      return this[action]($.extend({}, action in _default ? _default[action] : {}, todo.args ? todo.args : todo));
    }
    
    /**
     * called when action in finished, to acknoledge the current in stack and start next one
     **/
    this._end = function(){
      running = false;
      stack.ack();
      this._run();
    },
    /**
     * if not running, start next action in stack
     **/
    this._run = function(){
      if (running) {
        return;
      }
      var todo = stack.get();
      if (!todo) {
        return;
      }
      running = true;
      this._proceed(todo);
    }
    
    //-----------------------------------------------------------------------//
    // Call tools
    //-----------------------------------------------------------------------//
    
    /**
     * run the appropriated function
     **/
    this._proceed = function(todo){
      todo = todo || {};
      var action = ival(todo, 'action') || 'init',
          iaction = action.toLowerCase(),
          ok = true,
          target = ival(todo, 'target'), 
          args = ival(todo, 'args'),
          out;
      // check if init should be run automatically
      if ( !map && autoInit(iaction) ){
        this.init($.extend({}, _default.init, todo.args && todo.args.map ? todo.args.map : todo.map ? todo.map : {}), true);
      }
      
      // gmap3 function
      if (!target && !args && (iaction in this) && (typeof(this[iaction]) === 'function')){
        this[iaction]($.extend({}, iaction in _default ? _default[iaction] : {}, todo.args ? todo.args : todo)); // call fnc and extends defaults data
      } else {
        // "target" object function
        if (target && (typeof(target) === 'object')){
          if (ok = (typeof(target[action]) === 'function')){
            out = target[action].apply(target, todo.args ? todo.args : []);
          }
        // google.maps.Map direct function :  no result so not rewrited, directly wrapped using array "args" as parameters (ie. setOptions, addMapType, ...)
        } else if (map){
          if (ok = (typeof(map[action]) === 'function')){
            out = map[action].apply(map, todo.args ? todo.args : [] );
          }
        }
        if (!ok && _default.verbose) {
          alert("unknown action : " + action);
        }
        this._callback(out, todo);
        this._end();
      }
    }
    
    /**
     * returns the geographical coordinates from an address and call internal or given method
     **/
     this._resolveLatLng = function(todo, method, all, attempt){
      var address = ival(todo, 'address'),
          params,
          that = this,
          fnc = typeof(method) === 'function' ? method : that[method];
      if ( address ){
        if (!attempt){ // convert undefined to int
          attempt = 0;
        }
        if (typeof(address) === 'object'){
          params = address;
        } else {
          params = {'address': address};
        }
        getGeocoder().geocode(
          params, 
          function(results, status) {
          if (status === google.maps.GeocoderStatus.OK){
            fnc.apply(that, [todo, all ? results : results[0].geometry.location]);
          } else if ( (status === google.maps.GeocoderStatus.OVER_QUERY_LIMIT) && (attempt < _default.queryLimit.attempt) ){
            setTimeout(function(){
                that._resolveLatLng(todo, method, all, attempt+1);
              },
              _default.queryLimit.delay + Math.floor(Math.random() * _default.queryLimit.random)
            );
          } else {
            if (_default.verbose){
              alert('Geocode error : ' + status);
            }
            fnc.apply(that, [todo, false]);;
          }
        }
      );
      } else {
        fnc.apply(that, [todo, toLatLng(todo, false, true)]);
      }
    }
    
    /**
     * returns the geographical coordinates from an array of object using "address" and call internal method
     **/
    this._resolveAllLatLng = function(todo, property, method){
      var that = this,
          i = -1,
          solveNext = function(){
            do{
              i++;
            }while( (i < todo[property].length) && !('address' in todo[property][i]) );
            if (i < todo[property].length){
              (function(todo){
                that._resolveLatLng(
                  todo,
                  function(todo, latLng){
                    todo.latLng = latLng;
                    solveNext.apply(that, []); // solve next or execute exit method
                  }
                );
              })(todo[property][i]);
            } else {
              that[method](todo);
            }
          };
      solveNext();
    }
    
    /**
     * call a function of framework or google map object of the instance
     **/
    this._call = function(/* fncName [, ...] */){
      var i, fname = arguments[0], args = [];
      if ( !arguments.length || !map || (typeof(map[fname]) !== 'function') ){
        return;
      }
      for(i=1; i<arguments.length; i++){
        args.push(arguments[i]);
      }
      return map[fname].apply(map, args);
    }
    
    /**
     * init if not and manage map subcall (zoom, center)
     **/
    this._subcall = function(todo, latLng){
      var opts = {};
      if (!todo.map) return;
      if (!latLng) {
        latLng = ival(todo.map, 'latlng');
      }
      if (!map){
        if (latLng) {
          opts = {center:latLng};
        }
        this.init($.extend({}, todo.map, opts), true);
      } else { 
        if (todo.map.center && latLng){
          this._call("setCenter", latLng);
        }
        if (todo.map.zoom !== undefined){
          this._call("setZoom", todo.map.zoom);
        }
        if (todo.map.mapTypeId !== undefined){
          this._call("setMapTypeId", todo.map.mapTypeId);
        }
      }
    }
    
    /**
     * attach an event to a sender 
     **/
    this._attachEvent = function(sender, name, fnc, data, once){
      google.maps.event['addListener'+(once?'Once':'')](sender, name, function(event) {
        fnc.apply($this, [sender, event, data]);
      });
    }
    
    /**
     * attach events from a container to a sender 
     * todo[
     *  events => { eventName => function, }
     *  onces  => { eventName => function, }  
     *  data   => mixed data         
     * ]
     **/
    this._attachEvents = function(sender, todo){
      var name;
      if (!todo) {
        return
      }
      if (todo.events){
        for(name in todo.events){
          if (typeof(todo.events[name]) === 'function'){
            this._attachEvent(sender, name, todo.events[name], todo.data, false);
          }
        }
      }
      if (todo.onces){
        for(name in todo.onces){
          if (typeof(todo.onces[name]) === 'function'){
            this._attachEvent(sender, name, todo.onces[name], todo.data, true);
          }
        }
      }
    }
    
    /**
     * execute callback functions 
     **/
    this._callback = function(result, todo){
      if (typeof(todo.callback) === 'function') {
        todo.callback.apply($this, [result]);
      } else if (typeof(todo.callback) === 'object') {
        for(var i=0; i<todo.callback.length; i++){
          if (typeof(todo.callback[i]) === 'function') {
            todo.callback[k].apply($this, [result]);
          }
        }
      }
    }
    
    /**
     * execute ending functions 
     **/
    this._manageEnd = function(result, todo, internal){
      var i, apply;
      if (result && (typeof(result) === 'object')){
        // attach events
        this._attachEvents(result, todo);
        // execute "apply"
        if (todo.apply && todo.apply.length){
          for(i=0; i<todo.apply.length; i++){
            apply = todo.apply[i];
            // need an existing "action" function in the result object
            if(!apply.action || (typeof(result[apply.action]) !== 'function') ) { 
              continue;
            }
            if (apply.args) {
              result[apply.action].apply(result, apply.args);
            } else {
              result[apply.action]();
            }
          }
        }
      }
      if (!internal) {
        this._callback(result, todo);
        this._end();
      }
    }
    
    //-----------------------------------------------------------------------//
    // gmap3 functions
    //-----------------------------------------------------------------------//
    
    /**
     * destroy an existing instance
     **/
    this.destroy = function(todo){
      var k;
      store.clear();
      $this.empty();
      for(k in styles){
        delete styles[ k ];
      }
      styles = {};
      if (map){
        delete map;
      }
      this._callback(null, todo);
      this._end();
    }
    
    /**
     * Initialize google.maps.Map object
     **/
    this.init = function(todo, internal){
      var o, k, opts;
      if (map) { // already initialized
        return this._end();
      }
      o = getObject('map', todo);
      if ( (typeof(o.options.center) === 'boolean') && o.options.center) {
        return false; // wait for an address resolution
      }
      opts = $.extend({}, _default.init, o.options);
      if (!opts.center) {
        opts.center = [_default.init.center.lat, _default.init.center.lng];
      }
      opts.center = toLatLng(opts.center);
      map = new _default.classes.Map($this.get(0), opts);
      
      // add previous added styles
      for(k in styles) {
        map.mapTypes.set(k, styles[k]);
      }
      
      this._manageEnd(map, o, internal);
      return true;
    }
    
    /**
     * returns the geographical coordinates from an address
     **/
    this.getlatlng = function(todo){
      this._resolveLatLng(todo, '_getLatLng', true);
    },
    
    this._getLatLng = function(todo, results){
      this._manageEnd(results, todo);
    },
    
    
    /**
     * returns address from latlng        
     **/
    this.getaddress = function(todo, attempt){
      var latLng = toLatLng(todo, false, true),
          address = ival(todo, 'address'),
          params = latLng ?  {latLng:latLng} : ( address ? (typeof(address) === 'string' ? {address:address} : address) : null),
          callback = ival(todo, 'callback'),
          that = this;
      if (!attempt){ // convert undefined to int
        attempt = 0;
      }
      if (params && typeof(callback) === 'function') {
        getGeocoder().geocode(
          params, 
          function(results, status) {
            if ( (status === google.maps.GeocoderStatus.OVER_QUERY_LIMIT) && (attempt < _default.queryLimit.attempt) ){
              setTimeout(function(){
                  that.getaddress(todo, attempt+1);
                },
                _default.queryLimit.delay + Math.floor(Math.random() * _default.queryLimit.random)
              );
            } else {
              var out = status === google.maps.GeocoderStatus.OK ? results : false;
              callback.apply($this, [out, status]);
              if (!out && _default.verbose){
                alert('Geocode error : ' + status);
              }
              that._end();
            }
          } 
        );
      } else {
        this._end();
      }
    }
    
    /**
     * return a route
     **/
    this.getroute = function(todo){
      var callback = ival(todo, 'callback'),
          that = this;
      if ( (typeof(callback) === 'function') && todo.options ) {
        todo.options.origin = toLatLng(todo.options.origin, true);
        todo.options.destination = toLatLng(todo.options.destination, true);
        getDirectionsService().route(
          todo.options,
          function(results, status) {
            var out = status == google.maps.DirectionsStatus.OK ? results : false;
            callback.apply($this, [out, status]);
            that._end();
          }
        );
      } else {
        this._end();
      }
    }
    
    /**
     * return the elevation of a location
     **/
    this.getelevation = function(todo){
      var fnc, path, samples, i,
          locations = [],
          callback = ival(todo, 'callback'),
          latLng = ival(todo, 'latlng'),
          that = this;
          
      if (typeof(callback) === 'function'){
        fnc = function(results, status){
          var out = status === google.maps.ElevationStatus.OK ? results : false;
          callback.apply($this, [out, status]);
          that._end();
        };
        if (latLng){
          locations.push(toLatLng(latLng));
        } else {
          locations = ival(todo, 'locations') || [];
          if (locations){
            locations = array(locations);
            for(i=0; i<locations.length; i++){
              locations[i] = toLatLng(locations[i]);
            }
          }
        }
        if (locations.length){
          getElevationService().getElevationForLocations({locations:locations}, fnc);
        } else {
          path = ival(todo, 'path');
          samples = ival(todo, 'samples');
          if (path && samples){
            for(i=0; i<path.length; i++){
              locations.push(toLatLng(path[i]));
            }
            if (locations.length){
              getElevationService().getElevationAlongPath({path:locations, samples:samples}, fnc);
            }
          }
        }
      } else {
        this._end();
      }
    }
    
    /**
     * return the distance between an origin and a destination
     *      
     **/
    this.getdistance = function(todo){
      var i, 
          callback = ival(todo, 'callback'),
          that = this;
      if ( (typeof(callback) === 'function') && todo.options && todo.options.origins && todo.options.destinations ) {
        // origins and destinations are array containing one or more address strings and/or google.maps.LatLng objects
        todo.options.origins = array(todo.options.origins);
        for(i=0; i<todo.options.origins.length; i++){
          todo.options.origins[i] = toLatLng(todo.options.origins[i], true);
        }
        todo.options.destinations = array(todo.options.destinations);
        for(i=0; i<todo.options.destinations.length; i++){
          todo.options.destinations[i] = toLatLng(todo.options.destinations[i], true);
        }
        getDistanceMatrixService().getDistanceMatrix(
          todo.options,
          function(results, status) {
            var out = status == google.maps.DistanceMatrixStatus.OK ? results : false;
            callback.apply($this, [out, status]);
            that._end();
          }
        );
      } else {
        this._end();
      }
    }
    
    /**
     * Add a marker to a map after address resolution
     * if [infowindow] add an infowindow attached to the marker   
     **/
    this.addmarker = function(todo){
      this._resolveLatLng(todo, '_addMarker');
    }
    
    this._addMarker = function(todo, latLng, internal){
      var result, oi, to,
          o = getObject('marker', todo, 'to');
      if (!internal){
        if (!latLng) {
          this._manageEnd(false, o);
          return;
        }
        this._subcall(todo, latLng);
      } else if (!latLng){
        return;
      }
      if (o.to){
        to = store.refToObj(o.to);
        result = to && (typeof(to.add) === 'function');
        if (result){
          to.add(latLng, todo);
          if (typeof(to.redraw) === 'function'){
            to.redraw();
          }
        }
        if (!internal){
          this._manageEnd(result, o);
        }
      } else {
        o.options.position = latLng;
        o.options.map = map;
        result = new _default.classes.Marker(o.options);
        if (hasKey(todo, 'infowindow')){
          oi = getObject('infowindow', todo['infowindow'], 'open');
          // if "open" is not defined, add it in first position
          if ( (oi.open === undefined) || oi.open ){
            oi.apply = array(oi.apply);
            oi.apply.unshift({action:'open', args:[map, result]});
          }
          oi.action = 'addinfowindow';
          this._planNext(oi); 
        }
        if (!internal){
          store.add('marker', result, o);
          this._manageEnd(result, o);
        }
      }
      return result;
    }
    
    /**
     * add markers (without address resolution)
     **/
    this.addmarkers = function(todo){
      if (ival(todo, 'clusters')){
        this._resolveAllLatLng(todo, 'markers', '_addclusteredmarkers');
      } else {
        this._resolveAllLatLng(todo, 'markers', '_addmarkers');
      }
    }
    
    this._addmarkers = function(todo){
      var result, o, i, latLng, marker, options = {}, tmp, to, 
          markers = ival(todo, 'markers');
      this._subcall(todo);
      if (typeof(markers) !== 'object') {
        return this._end();
      }
      o = getObject('marker', todo, ['to', 'markers']);
      
      if (o.to){
        to = store.refToObj(o.to);
        result = to && (typeof(to.add) === 'function');
        if (result){
          for(i=0; i<markers.length; i++){
            if (latLng = toLatLng(markers[i])) {
              to.add(latLng, markers[i]);
            }
          }
          if (typeof(to.redraw) === 'function'){
            to.redraw();
          }
        }
        this._manageEnd(result, o);
      } else {
        $.extend(true, options, o.options);
        options.map = map;
        result = [];
        for(i=0; i<markers.length; i++){
          if (latLng = toLatLng(markers[i])){
            if (markers[i].options){
              tmp = {};
              $.extend(true, tmp, options, markers[i].options);
              o.options = tmp;
            } else {
              o.options = options;
            }
            o.options.position = latLng;
            marker = new _default.classes.Marker(o.options);
            result.push(marker);
            o.data = markers[i].data;
            o.tag = markers[i].tag;
            store.add('marker', marker, o);
            this._manageEnd(marker, o, true);
          }
        }
        o.options = options; // restore previous for futur use
        this._callback(result, todo);
        this._end();
      }
    }
    
    this._addclusteredmarkers = function(todo){
      var clusterer, i, latLng, storeId,
          that = this,
          radius = ival(todo, 'radius'),
          maxZoom = ival(todo, 'maxZoom'),
          markers = ival(todo, 'markers'),
          styles = ival(todo, 'clusters');
      
      if (!map.getBounds()){ // map not initialised => bounds not available
        // wait for map
        google.maps.event.addListenerOnce(
          map, 
          'bounds_changed', 
          function() {
            that._addclusteredmarkers(todo);
          }
        );
        return;
      }
      
      if (typeof(radius) === 'number'){
        clusterer = new Clusterer();
        for(i=0 ; i<markers.length; i++){
          latLng = toLatLng(markers[i]);
          clusterer.add(latLng, markers[i]);
        }
        storeId = this._initClusters(todo, clusterer, radius, maxZoom, styles);
      }
      
      this._callback(storeId, todo);
      this._end();
    }
    
    
    this._initClusters = function(todo, clusterer, radius, maxZoom, styles){
      var that = this;
      
      clusterer.setRedraw(function(force){
        var same, clusters = clusterer.clusters(map, radius, maxZoom, force);
        if (clusters){
          same = clusterer.freeDiff(clusters);
          that._displayClusters(todo, clusterer, clusters, same, styles);
        }
      });
      
      clusterer.events(
        google.maps.event.addListener(
          map, 
          'zoom_changed',
          function() {
            clusterer.redraw(true);
          }
        ),
        google.maps.event.addListener(
          map, 
          'bounds_changed',
          function() {
            clusterer.redraw();
          }
        )
      );
      
      clusterer.redraw();
      return store.add('cluster', clusterer, todo);
    }
    
    this._displayClusters = function(todo, clusterer, clusters, same, styles){
      var k, i, ii, m, done, obj, shadow, cluster, options, tmp, w, h,
          atodo,
          ctodo = hasKey(todo, 'cluster') ? getObject('', ival(todo, 'cluster')) : {},
          mtodo = hasKey(todo, 'marker') ? getObject('', ival(todo, 'marker')) : {};
      for(i=0; i<clusters.length; i++){
        if (i in same){
          continue;
        }
        cluster = clusters[i];
        done = false;
        if (cluster.idx.length > 1){
          // look for the cluster design to use
          m = 0;
          for(k in styles){
            if ( (k > m) && (k <= cluster.idx.length) ){
              m = k;
            }
          }
          if (styles[m]){ // cluster defined for the current markers count
            w = ival(styles[m], 'width');
            h = ival(styles[m], 'height');
            
            // create a custom _addOverlay command
            atodo = {};
            $.extend(
              true, 
              atodo, 
              ctodo, 
              { options:{
                  pane: 'overlayLayer',
                  content:styles[m].content.replace('CLUSTER_COUNT', cluster.idx.length),
                  offset:{
                    x: -w/2,
                    y: -h/2
                  }
                }
              }
            );
            obj = this._addOverlay(atodo, toLatLng(cluster), true);
            atodo.options.pane = 'floatShadow';
            atodo.options.content = $('<div></div>');
            atodo.options.content.width(w);
            atodo.options.content.height(h);
            shadow = this._addOverlay(atodo, toLatLng(cluster), true);
            
            // store data to the clusterer
            ctodo.data = {
              latLng: toLatLng(cluster),
              markers:[]
            };
            for(ii=0; ii<cluster.idx.length; ii++){
              ctodo.data.markers.push(
                clusterer.get(cluster.idx[ii]).marker
              );
            }
            this._attachEvents(shadow, ctodo);
            clusterer.store(cluster, obj, shadow);
            done = true;
          }
        }
        if (!done){ // cluster not defined (< min count) or = 1 so display all markers of the current cluster
          // save the defaults options for the markers
          options = {};
          $.extend(true, options, mtodo.options);
          for(ii = 0; ii <cluster.idx.length; ii++){
            m = clusterer.get(cluster.idx[ii]);
            mtodo.latLng = m.latLng;
            mtodo.data = m.marker.data;
            mtodo.tag = m.marker.tag;
            if (m.marker.options){
              tmp = {};
              $.extend(true, tmp, options, m.marker.options);
              mtodo.options = tmp;
            } else {
              mtodo.options = options;
            }
            obj = this._addMarker(mtodo, mtodo.latLng, true);
            this._attachEvents(obj, mtodo);
            clusterer.store(cluster, obj);
          }
          mtodo.options = options; // restore previous for futur use
        }
      }
    }
    
    /**
     * add an infowindow after address resolution
     **/
    this.addinfowindow = function(todo){ 
      this._resolveLatLng(todo, '_addInfoWindow');
    }
    
    this._addInfoWindow = function(todo, latLng){
      var o, infowindow, args = [];
      this._subcall(todo, latLng);
      o = getObject('infowindow', todo, ['open', 'anchor']);
      if (latLng) {
        o.options.position = latLng;
      }
      infowindow = new _default.classes.InfoWindow(o.options);
      if ( (o.open === undefined) || o.open ){
        o.apply = array(o.apply);
        args.push(map);
        if (o.anchor){
          args.push(o.anchor);
        }
        o.apply.unshift({action:'open', args:args});
      }
      store.add('infowindow', infowindow, o);
      this._manageEnd(infowindow, o);
    }
    
    
    /**
     * add a polygone / polylin on a map
     **/
    this.addpolyline = function(todo){
      this._addPoly(todo, 'Polyline', 'path');
    }
    
    this.addpolygon = function(todo){
      this._addPoly(todo, 'Polygon', 'paths');
    }
    
    this._addPoly = function(todo, poly, path){
      var i, 
          obj, latLng, 
          o = getObject(poly.toLowerCase(), todo, path);
      if (o[path]){
        o.options[path] = [];
        for(i=0; i<o[path].length; i++){
          if (latLng = toLatLng(o[path][i])){
            o.options[path].push(latLng);
          }
        }
      }
      obj = new google.maps[poly](o.options);
      obj.setMap(map);
      store.add(poly.toLowerCase(), obj, o);
      this._manageEnd(obj, o);
    }
    
    /**
     * add a circle   
     **/
    this.addcircle = function(todo){
      this._resolveLatLng(todo, '_addCircle');
    }
    
    this._addCircle = function(todo, latLng){
      var c, o = getObject('circle', todo);
      if (!latLng) {
        latLng = toLatLng(o.options.center);
      }
      if (!latLng) {
        return this._manageEnd(false, o);
      }
      this._subcall(todo, latLng);
      o.options.center = latLng;
      o.options.map = map;
      c = new _default.classes.Circle(o.options);
      store.add('circle', c, o);
      this._manageEnd(c, o);
    }
    
    /**
     * add a rectangle   
     **/
    this.addrectangle = function(todo){
      this._resolveLatLng(todo, '_addRectangle');
    }
    
    this._addRectangle = function(todo, latLng ){
      var r, o = getObject('rectangle', todo);
      o.options.bounds = toLatLngBounds(o.options.bounds, true);
      if (!o.options.bounds) {
        return this._manageEnd(false, o);
      }
      this._subcall(todo, o.options.bounds.getCenter());
      o.options.map = map;
      r = new _default.classes.Rectangle(o.options);
      store.add('rectangle', r, o);
      this._manageEnd(r, o);
    }    
    
    /**
     * add an overlay to a map after address resolution
     **/
    this.addoverlay = function(todo){
      this._resolveLatLng(todo, '_addOverlay');
    }
    
    this._addOverlay = function(todo, latLng, internal){
      var ov,  
          o = getObject('overlay', todo),
          opts =  $.extend({
                    pane: 'floatPane',
                    content: '',
                    offset:{
                      x:0,y:0
                    }
                  },
                  o.options),
          $div = $('<div></div>'),
          listeners = [];
       
       $div
          .css('border', 'none')
          .css('borderWidth', '0px')
          .css('position', 'absolute');
        $div.append(opts.content);
      
      function f() {
       _default.classes.OverlayView.call(this);
        this.setMap(map);
      }            
      
      f.prototype = new _default.classes.OverlayView();
      
      f.prototype.onAdd = function() {
        var panes = this.getPanes();
        if (opts.pane in panes) {
          $(panes[opts.pane]).append($div);
        }
      }
      f.prototype.draw = function() {
        var overlayProjection = this.getProjection(),
            ps = overlayProjection.fromLatLngToDivPixel(latLng),
            that = this;
            
        $div
          .css('left', (ps.x+opts.offset.x) + 'px')
          .css('top' , (ps.y+opts.offset.y) + 'px');
        
        $.each( ("dblclick click mouseover mousemove mouseout mouseup mousedown").split(" "), function( i, name ) {
          listeners.push(
            google.maps.event.addDomListener($div[0], name, function(e) {
              google.maps.event.trigger(that, name);
            })
          );
        });
        listeners.push(
          google.maps.event.addDomListener($div[0], "contextmenu", function(e) {
            google.maps.event.trigger(that, "rightclick");
          })
        );
      }
      f.prototype.onRemove = function() {
        for (var i = 0; i < listeners.length; i++) {
          google.maps.event.removeListener(listeners[i]);
        }
        $div.remove();
      }
      f.prototype.hide = function() {
        $div.hide();
      }
      f.prototype.show = function() {
        $div.show();
      }
      f.prototype.toggle = function() {
        if ($div) {
          if ($div.is(':visible')){
            this.show();
          } else {
            this.hide();
          }
        }
      }
      f.prototype.toggleDOM = function() {
        if (this.getMap()) {
          this.setMap(null);
        } else {
          this.setMap(map);
        }
      }
      f.prototype.getDOMElement = function() {
        return $div[0];
      }
      ov = new f();
      if (!internal){
        store.add('overlay', ov, o);
        this._manageEnd(ov, o);
      }
      return ov;
    }
    
    /**
     * add a fix panel to a map
     **/
    this.addfixpanel = function(todo){
      var o = getObject('fixpanel', todo),
          x=y=0, $c, $div;
      if (o.options.content){
        $c = $(o.options.content);
        
        if (o.options.left !== undefined){
          x = o.options.left;
        } else if (o.options.right !== undefined){
          x = $this.width() - $c.width() - o.options.right;
        } else if (o.options.center){
          x = ($this.width() - $c.width()) / 2;
        }
        
        if (o.options.top !== undefined){
          y = o.options.top;
        } else if (o.options.bottom !== undefined){
          y = $this.height() - $c.height() - o.options.bottom;
        } else if (o.options.middle){
          y = ($this.height() - $c.height()) / 2
        }
      
        $div = $('<div></div>')
                .css('position', 'absolute')
                .css('top', y+'px')
                .css('left', x+'px')
                .css('z-index', '1000')
                .append($c);
        
        $this.first().prepend($div);
        this._attachEvents(map, o);
        store.add('fixpanel', $div, o);
        this._callback($div, o);
      }
      this._end();
    }
    
    /**
     * add a direction renderer to a map
     **/
    this.adddirectionsrenderer = function(todo, internal){
      var dr, o = getObject('directionrenderer', todo, 'panelId');
      o.options.map = map;
      dr = new google.maps.DirectionsRenderer(o.options);
      if (o.panelId) {
        dr.setPanel(document.getElementById(o.panelId));
      }
      store.add('directionrenderer', dr, o);
      this._manageEnd(dr, o, internal);
      return dr;
    }
    
    /**
     * set a direction panel to a dom element from its ID
     **/
    this.setdirectionspanel = function(todo){
      var dr = store.get('directionrenderer'),
          o = getObject('directionpanel', todo, 'id');
      if (dr && o.id) {
        dr.setPanel(document.getElementById(o.id));
      }
      this._manageEnd(dr, o);
    }
    
    /**
     * set directions on a map (create Direction Renderer if needed)
     **/
    this.setdirections = function(todo){
      var dr = store.get('directionrenderer'),
          o = getObject('directions', todo);
      if (todo) {
        o.options.directions = todo.directions ? todo.directions : (todo.options && todo.options.directions ? todo.options.directions : null);
      }
      if (o.options.directions) {
        if (!dr) {
          dr = this.adddirectionsrenderer(o, true);
        } else {
          dr.setDirections(o.options.directions);
        }
      }
      this._manageEnd(dr, o);
    }
    
    /**
     * set a streetview to a map
     **/
    this.setstreetview = function(todo){
      var panorama,
          o = getObject('streetview', todo, 'id');
      if (o.options.position){
        o.options.position = toLatLng(o.options.position);
      }
      panorama = new _default.classes.StreetViewPanorama(document.getElementById(o.id),o.options);
      if (panorama){
        map.setStreetView(panorama);
      }
      this._manageEnd(panorama, o);
    }
    
    /**
     * add a kml layer to a map
     **/
    this.addkmllayer = function(todo){
      var kml,
          o = getObject('kmllayer', todo, 'url');
      o.options.map = map;
      if (typeof(o.url) === 'string'){
        kml = new _default.classes.KmlLayer(o.url, o.options);
      }
      store.add('kmllayer', kml, o);
      this._manageEnd(kml, o);
    }
    
    /**
     * add a traffic layer to a map
     **/
    this.addtrafficlayer = function(todo){
      var o = getObject('trafficlayer', todo),
          tl = store.get('trafficlayer');
      if (!tl){
        tl = new _default.classes.TrafficLayer();
        tl.setMap(map);
        store.add('trafficlayer', tl, o);
      }
      this._manageEnd(tl, o);
    }
    
    /**
     * add a bicycling layer to a map
     **/
    this.addbicyclinglayer = function(todo){
      var o = getObject('bicyclinglayer', todo),
          bl = store.get('bicyclinglayer');
      if (!bl){
        bl = new _default.classes.BicyclingLayer();
        bl.setMap(map);
        store.add('bicyclinglayer', bl, o);
      }
      this._manageEnd(bl, o);
    }
    
    /**
     * add a ground overlay to a map
     **/
    this.addgroundoverlay = function(todo){
      var ov,
          o = getObject('groundoverlay', todo, ['bounds', 'url']);
      o.bounds = toLatLngBounds(o.bounds);
      if (o.bounds && (typeof(o.url) === 'string')){
        ov = new _default.classes.GroundOverlay(o.url, o.bounds);
        ov.setMap(map);
        store.add('groundoverlay', ov, o);
      }
      this._manageEnd(ov, o);
    }
    
    /**
     * geolocalise the user and return a LatLng
     **/
    this.geolatlng = function(todo){
      var callback = ival(todo, 'callback');
      if (typeof(callback) === 'function') {
        if(navigator.geolocation) {
          navigator.geolocation.getCurrentPosition(
            function(position) {
              var out = new google.maps.LatLng(position.coords.latitude,position.coords.longitude);
              callback.apply($this, [out]);
            }, 
            function() {
              var out = false;
              callback.apply($this, [out]);
            }
          );
        } else if (google.gears) {
          google.gears.factory.create('beta.geolocation').getCurrentPosition(
            function(position) {
              var out = new google.maps.LatLng(position.latitude,position.longitude);
              callback.apply($this, [out]);
            }, 
            function() {
              out = false;
              callback.apply($this, [out]);
            }
          );
        } else {
          callback.apply($this, [false]);
        }
      }
      this._end();
    }
    
    /**
     * add a style to a map
     **/
    this.addstyledmap = function(todo, internal){
      var o = getObject('styledmap', todo, ['id', 'style']);
      if  (o.style && o.id && !styles[o.id]) {
        styles[o.id] = new _default.classes.StyledMapType(o.style, o.options);
        if (map) {
          map.mapTypes.set(o.id, styles[o.id]);
        }
      }
      this._manageEnd(styles[o.id], o, internal);
    }
    
    /**
     * set a style to a map (add it if needed)
     **/
    this.setstyledmap = function(todo){
      var o = getObject('styledmap', todo, ['id', 'style']);
      if (o.id) {
        this.addstyledmap(o, true);
        if (styles[o.id]) {
          map.setMapTypeId(o.id);
          this._callback(styles[o.id], todo);
        }
      }
      this._manageEnd(styles[o.id], o);
    }
    
    /**
     * remove objects from a map
     **/
    this.clear = function(todo){
      var list = array(ival(todo, 'list') || ival(todo, 'name')),
          last = ival(todo, 'last', false),
          first = ival(todo, 'first', false),
          tag = ival(todo, 'tag');
      if (tag !== undefined){
        tag = array(tag);
      }
      store.clear(list, last, first, tag);
      this._end();
    }
    
    /**
     * return objects previously created
     **/
    this.get = function(todo){
      var name = ival(todo, 'name') || 'map',
          first= ival(todo, 'first'),
          all  = ival(todo, 'all'),
          tag = ival(todo, 'tag');
      name = name.toLowerCase();
      if (name === 'map'){
        return map;
      }
      if (tag !== undefined){
        tag = array(tag);
      }
      if (first){
        return store.get(name, false, tag);
      } else if (all){
        return store.all(name, tag);
      } else {
        return store.get(name, true, tag);
      }
    }
    
    /**
     * return the max zoom of a location
     **/
    this.getmaxzoom = function(todo){
      this._resolveLatLng(todo, '_getMaxZoom');
    }
    
    this._getMaxZoom = function(todo, latLng){
      var callback = ival(todo, 'callback'),
          that = this;
      if (callback && typeof(callback) === 'function') {
        getMaxZoomService().getMaxZoomAtLatLng(
          latLng, 
          function(result) {
            var zoom = result.status === google.maps.MaxZoomStatus.OK ? result.zoom : false;
            callback.apply($this, [zoom, result.status]);
            that._end();
          }
        );
      } else {
        this._end();
      }
    }
    
    /**
     * modify default values
     **/
    this.setdefault = function(todo){
      setDefault(todo);
      this._end();
    }
    
    /**
     * autofit a map using its overlays (markers, rectangles ...)
     **/
    this.autofit = function(todo, internal){
      var names, list, obj, i, j,
          empty = true, 
          bounds = new google.maps.LatLngBounds(),
          maxZoom = ival(todo, 'maxZoom', null);

      names = store.names();
      for(i=0; i<names.length; i++){
        list = store.all(names[i]);
        for(j=0; j<list.length; j++){
          obj = list[j];
          if (obj.getPosition){
            bounds.extend(obj.getPosition());
            empty = false;
          } else if (obj.getBounds){
            bounds.extend(obj.getBounds().getNorthEast());
            bounds.extend(obj.getBounds().getSouthWest());
            empty = false;
          } else if (obj.getPaths){
            obj.getPaths().forEach(function(path){
              path.forEach(function(latLng){
                bounds.extend(latLng);
                empty = false;
              });
            });
          } else if (obj.getPath){
            obj.getPath().forEach(function(latLng){
              bounds.extend(latLng);
              empty = false;
            });
          } else if (obj.getCenter){
            bounds.extend(obj.getCenter());
            empty = false;
          }
        }
      }

      if (!empty && (!map.getBounds() || !map.getBounds().equals(bounds))){
        if (maxZoom !== null){  
          // fitBouds Callback event => detect zoom level and check maxZoom
          google.maps.event.addListenerOnce(
            map, 
            'bounds_changed', 
            function() {
              if (this.getZoom() > maxZoom){
                this.setZoom(maxZoom);
              }
            }
          );
        }
        map.fitBounds(bounds);
      }
      if (!internal){
        this._manageEnd(empty ? false : bounds, todo, internal);
      }
    }
    
  };
  
  //-----------------------------------------------------------------------//
  // jQuery plugin
  //-----------------------------------------------------------------------//
    
  $.fn.gmap3 = function(){
    var i, args, list = [], empty = true, results = [];
    // store all arguments in a todo list 
    for(i=0; i<arguments.length; i++){
      args = arguments[i] || {};
      // resolve string todo - action without parameters can be simplified as string 
      if (typeof(args) === 'string'){
        args = {action:args};
      }
      list.push(args);
    }
    // resolve empty call - run init
    if (!list.length) {
      list.push({});
    }
    // loop on each jQuery object
    $.each(this, function() {
      var $this = $(this),
          gmap3 = $this.data('gmap3');
      empty = false;
      if (!gmap3){
        gmap3 = new Gmap3($this);
        $this.data('gmap3', gmap3);
      }
      // direct call : bypass jQuery method (not stackable, return mixed)
      if ( (list.length == 1) && (isDirect(list[0])) ){
        results.push(gmap3._direct(list[0]));
      } else {
        gmap3._plan(list);
      }
    });
    // return for direct call (only) 
    if (results.length){
      if (results.length === 1){ // 1 css selector
        return results[0];
      } else {
        return results;
      }
    }
    // manage setDefault call
    if (empty && (arguments.length == 2) && (typeof(arguments[0]) === 'string') && (arguments[0].toLowerCase() === 'setdefault')){
      setDefault(arguments[1]);
    }
    return this;
  }

}(jQuery));
