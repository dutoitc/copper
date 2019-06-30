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

    $scope.sortBy='k';
    $scope.sortReverse=false;

    $scope.triggerSort = function(value) {
        if (value==$scope.sortBy) {
            $scope.sortReverse = !$scope.sortReverse;
        } else {
            $scope.sortReverse = false;
            $scope.sortBy = value;
        }
    }


      $http.get('/ws/values')
            .then(function(response) {
                //$scope.values=response.data;
                console.log(response.data);
             var values = [];
             Object.keys(response.data).forEach(function(key) {
                console.log("Adding", response.data[key]);
                values.push(response.data[key]);
             });
             $scope.values = values;
             console.log(values);
        });


      $http.get('/ws/values/alerts')
            .then(function(response) {
                $scope.alerts=response.data;
        });



    $scope.filterOLD = function(object, field, filter) {
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