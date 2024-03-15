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
    var postValueKey="";
    var postValue = "";

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


    $scope.refresh = function() {
        $http.get('../ws/values')
            .then(function (response) {
                //$scope.values=response.data;
                console.log(response.data);
                var values = [];
                Object.keys(response.data).forEach(function (key) {
                    console.log("Adding", response.data[key]);
                    values.push(response.data[key]);
                });
                $scope.values = values;
                console.log(values);
            });


        $http.get('../ws/values/alerts')
            .then(function (response) {
                $scope.alerts = response.data;
            });
    }

    $scope.refresh();



    $scope.deleteValuesOlderThanOneMonth = function() {
        if ( window.confirm("Delete values older than one month ?") ) {
            $http.delete('../ws/admin/values/olderThanOneMonth')
                .then(function(response) {
                    alert(response.data);
                    $scope.refresh();
                });
        }
    }

    $scope.deleteValuesOlderThanThreeMonth = function() {
        if ( window.confirm("Delete values older than three month ?") ) {
            $http.delete('../ws/admin/values/olderThanThreeMonth')
                .then(function(response) {
                    alert(response.data);
                    $scope.refresh();
                });
        }
    }


    $scope.deleteValuesOfKey = function(key) {
        if ( window.confirm("Delete all values of key " + key + " ?") ) {
            $http.delete('../ws/admin/values/bykey/' + key)
                .then(function(response) {
                    alert(response.data);
                    $scope.refresh();
                });
        }
    }

    $scope.deleteDuplicates = function(key) {
        if ( window.confirm("Delete duplicates ?") ) {
            $http.delete('../ws/admin/values/duplicates')
                .then(function(response) {
                    alert(response.data);
                    $scope.refresh();
                });
        }
    }



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

    $scope.doPostValue = function() {
        $http.post('../ws/admin/value/'+$scope.postValueKey, $scope.postValue)
            .then(
                function(data, status) {
                    if (data.data=="OK") {
                        alert("Value posted !");
                        $scope.refresh();
                    } else {
                        alert("Error: " + data.data);
                    }
                },
                function(data, status) {
                    alert("Exception: " + data.data);
                });
    }


}]);