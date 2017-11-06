'use strict';

// Declare app level module which depends on views, and components
var copperApp = angular.module('copperApp', [
  'ngRoute',
  'copperApp.overview',
  'copperApp.stories',
  'copperApp.story',
  'copperApp.values',
  'copperApp.history',
  'ui.bootstrap',
  'chart.js'
])
.config(['$routeProvider', '$httpProvider', '$locationProvider', '$qProvider', function($routeProvider, $httpProvider, $locationProvider, $qProvider) {
    $routeProvider
    .when('/help', {
        templateUrl: 'app/views/help.html'
    })
    .otherwise({redirectTo: '/overview'});
    $locationProvider.hashPrefix('');
    $qProvider.errorOnUnhandledRejections(false); // https://github.com/angular-ui/ui-router/issues/2889
}])
    .filter('trusted', ['$sce', function($sce){
        return function(text) {
            return $sce.trustAsHtml(text);
        };
}]);
