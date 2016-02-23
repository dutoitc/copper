'use strict';

// Declare app level module which depends on views, and components
var copperApp = angular.module('copperApp', [
  'ngRoute',
  'copperApp.overview',
  'copperApp.stories',
  'copperApp.story',
  'copperApp.values',
  'ui.bootstrap'
])
.config(['$routeProvider', '$httpProvider', function($routeProvider, $httpProvider) {
    $routeProvider
    .when('/help', {
        templateUrl: 'app/views/help.html'
    })
    .otherwise({redirectTo: '/overview'});
}])
    .filter('trusted', ['$sce', function($sce){
        return function(text) {
            return $sce.trustAsHtml(text);
        };
}]);

