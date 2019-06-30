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

      $http.get('/ws/values')
            .then(function(response) {
                $scope.values=response.data;
        });


      $http.get('/ws/values/alerts')
            .then(function(response) {
                $scope.alerts=response.data;
        });

        $scope.filter = function(object, field, filter) {
           if (!object) return {};
           if (!filter) return object;

           var filteredObject = {};
           var filterLower = filter.toLowerCase();
           Object.keys(object).forEach(function(key) {
             var textLower = object[key][field].toLowerCase();
             if (textLower.includes(filterLower)) {
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