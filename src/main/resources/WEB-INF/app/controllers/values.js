'use strict';

angular.module('copperApp.values', ['ngRoute'])
.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/values', {
    templateUrl: 'app/views/values.html',
    controller: 'ValuesCtrl'
  });
}])

.controller('ValuesCtrl', ['$http', '$scope', function($http, $scope) {
    var scope = $scope;
    var self=this;

      $http.get('ws/values')
            .success(function(data) {
                $scope.values=data;
        });

/*

    $http.get('data/routes.json')
        .success(function(data) {
            $scope.routes=data["routes"];
    });
    $http.get('data/processes.json')
        .success(function(data) {
            $scope.processes=data["processes"];
    });
    $http.get('data/services.json')
        .success(function(data) {
            $scope.services=data["services"];
    });*/

}]);