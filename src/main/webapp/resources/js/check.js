/**
 * Created by samuel on 02.11.16.
 */
var stomp = Stomp.over(new SockJS('/connect'));
stomp.debug = function (args) {
    console.log(args);
};
stomp.connect(header, function (frame) {
    console.log(frame);
    stomp.subscribe('/user/check', function (payload, headers, res) {
        console.log(payload);
        var event = JSON.parse(payload.body);
        if (event.message) {
            $('#log').append('</br>' + event.message);
        }
        executeEvent(event);
    }, header);
}, function (err) {
    console.error('connection error:' + err);
});

function executeEvent(event) {
    switch (event.typ) {
        case 'ResolverFinishedWithResult':
            setResolvedResult(event.result);
            break;
        case 'TesterFinished':
            setResultMalicious(event.malicious);
            break;
        case 'TestFinishedWithResult':
        case 'TestFinishedWithError':
            setTestResult(event);
            break;
        default:
            break;
    }
}

function setTestResult(event) {
    switch (event.test_name) {
        case 'VirusScan':
            setVirusScanResult(event.result);
            break;
        case 'PortScan':
            setPortScanResult(event.result);
            break;
        default:
            break;
    }
}

function setVirusScanResult(result) {
    setResultMaliciousImage($('#virusscan-state'), result.malicious);
    $('#virusscan-placeholder').addClass('invisible');
    $('#virusscan-info').html('Geprüfte Dateien: ' + result.info.scanned_files + '</br>Infizierte Dateien gefunden: ' + result.info.malicious_files).removeClass('invisible');
    var files = result.info.files;
    for (var i = 0; i < files.length; i++) {
        $('#virusscan-result').append('<tr' + (files[i].malicious ? ' class="table-danger"' : '') + '><td>' + files[i].name + '</td><td><span class="fa fa-' + (files[i].malicious ? 'exclamation-circle text-danger' : 'check-circle text-success') + '"></span></td></tr>');
    }
    $('#virusscan-result').removeClass('invisible');
}

function setPortScanResult(result) {
    setResultMaliciousImage($('#portscan-state'), result.malicious);
    $('#portscan-placeholder').addClass('invisible');
    $('#portscan-info').html('Keine verdächtigen Ports gefunden.').removeClass('invisible');
    var unknown_ports = result.info.unknown_ports;
    if (unknown_ports.length > 0) {
        $('#portscan-info').html('Folgende verdächtige Ports wurden abgefragt:');
        for (var i = 0; i < unknown_ports.length; i++) {
            $('#portscan-result').append('<tr><td>' + unknown_ports[i] + '</td></tr>');
        }
        $('#portscan-result').removeClass('invisible');
    }
}

function setResolvedResult(result) {
    if (result.reachable) {
        $('#resolved-url').html(result.resolved);
    } else {
        // TODO show error and cancel all views
    }
}

function setResultMaliciousImage(element, malicious) {
    if (malicious) {
        element.attr('src', 'img/error.png');
    } else {
        element.attr('src', 'img/success.png');
    }
}

function setResultMalicious(malicious) {
    $('#heading-log').css('background-color', '#f5f5f5');
    if (malicious) {
        $('#test-state').attr('src', 'img/webifier-error.png');
    } else {
        $('#test-state').attr('src', 'img/webifier-success.png');
    }
}
