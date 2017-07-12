// Define Angular Module
var app = angular.module("demoApp", []);

// Example of $rootScope
app.run(function($rootScope) {
    $rootScope.myColor = 'blue';
});


function demoCtrl() {
    this.showMessage = function(message) {
    	return 'Message - ' + message;
    };
    this.showResponse = function() {
    	return 'Response - ' + this.response + " - " + this.responseMsg;
    };
	this.title = "AngularJS Demo";
    this.book = "The Alchemist";
    this.author = "Paulo Coelho";
    this.message = "demoCtrl message";
    this.demohref = "index.html";
    this.demosrc = "img/file.png";
    this.response = "error";
    this.responseMsg = "Response message";
    this.showMenu = true;
    this.userExists = true;
    this.access = "admin";
    this.price = "123.45";
};

function userCtrl() {
    this.showMessage = function(message) {
    	return 'Message - ' + message;
    };
    
	this.title = "EDI User Mapping";
    this.entityId = "6276143";
    this.ticket = "EDI-11111";
    this.message = "user mapping message";
    this.mappings = [
    	{
	    	clId : '6123456',
	    	sso : 'sso123456'
	    }, {
	    	clId : '6153427',
	    	sso : 'sso871894'
	    }
    ];
    
    this.reset = function() {
    	this.entityId = "";
        this.ticket = "";
        this.mappings = [ {
        	clId : '',
        	sso : ''
        } ];
    };
    
    this.addUser = function() {
    	this.mappings.push(this.addMe);
    };
    
    this.removeUser = function(x) {
    	this.mappings.splice(x, 1);
    };
};

function servCtrl(UserService) {
	this.sayHello = function(name) {
		UserService.sayHello(name);
	};
}


function itemCtrl() {
	var vm = this;
	// function to remove an item
	vm.removeFromStock = function(item, index) {
		vm.items.splice(index, 1);
	};
	// function to add an item
	vm.addStock = function(item) {
		vm.items.push(item);
	};
	// define an empty object
	vm.form = {
		name : '',
		id : 0
	};
	
	vm.itemName="Snorkel";
	vm.items = [ {
		name : 'Scuba Diving Kit',
		id : 7297510
	}, {
		name : 'Snorkel',
		id : 0278916
	}, {
		name : 'Wet Suit',
		id : 2389017
	}, {
		name : 'Beach Towel',
		id : 1000983
	} ];
	
	// order by user input
	this.orderByMe = function(x) {
		this.myOrderBy = x;
	};
};


// ajax controllers
function studentController($scope, $http) {
	var url = "data/students.txt";

	$http.get(url).then( function(response) {
		$scope.students = response.data.records;
	});
}

function ajaxCtrl_1($scope, $http) {
	$http.get("header.html").then(function(response) {
		$scope.content = response.data;
    });
}

function ajaxCtrl_2($scope, $http) {
	$http({
        method : "GET",
        url : "header.html"
    }).then(function mySuccess(response) {
    	$scope.content = response.data;
    }, function myError(response) {
    	$scope.content = response.statusText;
    });
}

function ajaxCtrl($scope, $http) {
	$http.get("header.html")
    .then(function(response) {
        $scope.content = response.data;
        $scope.status = response.status;
        $scope.statustext = response.statustext;
        $scope.headers = response.headers;
        $scope.config = response.config;
    });
}

function ajaxCtrl_4($scope, $http) {
	$http.get("header.html")
	.then(function(response) {
        //First function handles success
        $scope.content = response.data;
    }, function(response) {
        //Second function handles error
        $scope.content = "Something went wrong";
    });
}
// End of ajax

// Service controllers
app.controller('getUrlCtrl', function($scope, $location) {
    $scope.myUrl = $location.absUrl();
});

app.controller('myTimeout', function($scope, $timeout) {
	$scope.myHeader = "Hello World!";
	$timeout(function () {
		$scope.myHeader = "How are you today?";
	}, 2000);
});

app.controller('myInterval', function($scope, $interval) {
    $scope.theTime = new Date().toLocaleTimeString();
    $interval(function () {
        $scope.theTime = new Date().toLocaleTimeString();
    }, 1000);
});

app.service('hexafy', function() {
    this.myFunc = function (x) {
        return x.toString(16);
    }
});

app.controller('myHexafy', function($scope, hexafy) {
  $scope.hex = hexafy.myFunc(255);
});


function UserService_Service() {
	this.sayHello = function(name) {
		return 'Hello there ' + name;
	};
}

function UserService() {
	var UserService = {};
	function greeting(name) {
		return 'Hello there ' + name;
	}
	UserService.sayHello = function(name) {
		return greeting(name);
	};
	return UserService;
}
// End of service


// Services
app.service('UserService', UserService);


// Controllers
app.controller("demoCtrl", demoCtrl);

app.controller("userCtrl", userCtrl);

app.controller("itemCtrl", itemCtrl);

app.controller("servCtrl", servCtrl);

app.controller("ajaxCtrl", ajaxCtrl);

app.controller("studentController", studentController);

// Custom directives
app.directive("myTestDirective1", function() {
    return {
        template : "<h1>Made by a directive!</h1>"
    };
});

app.directive("myTestDirective", function() {
	// The legal restrict values are:
	// E for Element name
	// A for Attribute
	// C for Class
	// M for Comment
	// By default the value is EA
    return {
        restrict : "EA",
        template : "<h1>Made by a directive!</h1>"
    };
});


// Custom filters
app.filter('myFormat', function() {
    return function(x) {
        var i, c, txt = "";
        for (i = 0; i < x.length; i++) {
            c = x[i];
            if (i % 2 == 0) {
                c = c.toUpperCase();
            }
            txt += c;
        }
        return txt;
    };
});

// Use a Custom Service Inside a Filter
app.filter('myToHex',['hexafy', function(hexafy) {
    return function(x) {
        return hexafy.myFunc(x);
    };
}]);


