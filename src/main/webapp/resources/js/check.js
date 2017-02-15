/**
 * Created by samuel on 02.11.16.
 */
var client = Stomp.over(new SockJS('/connect'));
client.connect(getHeader(), function (frame) {
    console.log(frame);
    client.subscribe('/user/started', function (payload) {
        $('#test-state').attr('src', 'img/webifier-loading.gif');
        $('#waiting-position').hide();
        $('#test-info').hide();
    }, getHeader());
    client.subscribe('/user/waiting', function (payload) {
        $('#waiting-position').html(payload.body);
    }, getHeader());
    client.subscribe('/user/check', function (payload) {
        var event = JSON.parse(payload.body);
        if (event.message) {
            $('#log').append('</br>' + event.message);
        }
        executeEvent(event);
    }, getHeader());
    client.send('/connect', header, 'connect');
}, function (err) {
    console.error('connection error:' + err);
});

function getHeader() {
    return JSON.parse(JSON.stringify(header));
}

function executeEvent(event) {
    switch (event.typ) {
        case 'ResolverFinished':
            setResolvedResult(event.result);
            break;
        case 'TestStarted':
            setTestLoading(event);
            break;
        case 'TestFinished':
            setTestResult(event);
            break;
        case 'TesterFinished':
            setResultMalicious(event.result);
            break;
        default:
            break;
    }
}

function setTestLoading(event) {
    switch (event.test_name) {
        case 'VirusScan':
            $('#virusscan-state').attr('src', 'img/loading.gif');
            break;
        case 'PortScan':
            $('#portscan-state').attr('src', 'img/loading.gif');
            break;
        case 'HeaderInspection':
            $('#header-inspection-state').attr('src', 'img/loading.gif');
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
        case 'HeaderInspection':
            setHeaderInspectionResult(event.result);
            break;
        default:
            break;
    }
}

function setVirusScanResult(result) {
    setResultMaliciousImage($('#virusscan-state'), result.result);
    $('#virusscan-placeholder').addClass('invisible');
    $('#virusscan-info').html('Geprüfte Dateien: ' + result.info.scanned_files + '</br>Verdächtige Dateien: ' + result.info.suspicious_files + '</br>Maliziöse Dateien: ' + result.info.malicious_files).removeClass('invisible');
    var files = result.info.files;
    for (var i = 0; i < files.length; i++) {
        var table = '';
        var icon = 'fa-check-circle';
        var color = 'text-success';
        if (files[i].result == "SUSPICIOUS") {
            table = 'table-warning';
            icon = 'fa-exclamation-triangle';
            color = 'text-warning';
        }
        if (files[i].result == "MALICIOUS") {
            table = 'table-danger';
            icon = 'fa-times-circle';
            color = 'text-danger';
        }
        $('#virusscan-result').append('<tr class="' + table + '"><td><small>' + files[i].name + '</small></td><td><span class="fa ' + icon + ' ' + color + '"></span></td></tr>');
    }
    $('#virusscan-result').removeClass('invisible');
}

function setPortScanResult(result) {
    setResultMaliciousImage($('#portscan-state'), result.result);
    $('#portscan-placeholder').addClass('invisible');
    var $portscan_info = $('#portscan-info');
    $portscan_info.html('Keine verdächtigen Ports gefunden.').removeClass('invisible');
    var unknown_ports = result.info.unknown_ports;
    if (unknown_ports.length > 0) {
        $portscan_info.html('Folgende verdächtige Ports wurden abgefragt:');
        var $portscan_result = $('#portscan-result');
        for (var i = 0; i < unknown_ports.length; i++) {
            $portscan_result.append('<tr><td>' + unknown_ports[i] + '</td></tr>');
        }
        $portscan_result.removeClass('invisible');
    }
}

function setHeaderInspectionResult(result) {
    setResultMaliciousImage($('#header-inspection-state'), result.result);
    $('#header-inspection-placeholder').addClass('invisible');
    var resultText = (result.info.malicious) ? "HTML-Antworten stimmen nicht überein" : "HTML-Antworten stimmen überein";
    $('#header-inspection-info').html(resultText).removeClass('invisible');
}

function setResolvedResult(result) {
    if (result.reachable) {
        $('#resolved-url').html(result.resolved);
    } else {
        // TODO show error and cancel all views
    }
}

function setResultMaliciousImage(element, result) {
    element.attr('src', 'img/' + getResultImage(result));
}

function setResultMalicious(result) {
    $('#heading-log').css('background-color', '#f5f5f5');
    $("#favicon").attr('href', 'img/webifier-' + getResultImage(result));
    $('#test-state').attr('src', 'img/webifier-' + getResultImage(result));
}

function getResultImage(result) {
    switch (result) {
        case "CLEAN":
            return 'clean.png';
        case "SUSPICIOUS":
            return 'warning.png';
        case "MALICIOUS":
            return 'malicious.png';
        default:
            return 'undefined.png';
    }
}
