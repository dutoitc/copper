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

        $scope.filter = function(object, field, filter) {
           if (!object) return {};
           if (!filter) return object;

           var filteredObject = {};
           Object.keys(object).forEach(function(key) {
             if (object[key][field].includes(filter)) {
               filteredObject[key] = object[key];
             }
           });

           return filteredObject;
         };

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