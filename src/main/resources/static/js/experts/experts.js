async function fetchProfileImage() {
    const token = localStorage.getItem('accessToken');

    const options = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        }
    };

    const response = await fetch('/api/v1/members/profile/images', options);

    if (response.ok) {
        const data = await response.json();
        const imageUrl = data.filenames[0];
        document.getElementById('profile-pic').style.backgroundImage = `url(${imageUrl})`;
    } else {
        document.getElementById('profile-pic').style.backgroundImage = `url('https://gosu-catcher.s3.ap-northeast-2.amazonaws.com/default.png')`;
    }
}

async function fetchExpertProfile() {
    const token = localStorage.getItem('accessToken');

    const options = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        }
    };

    const response = await fetch('/api/v1/experts', options);

    if (response.ok) {
        const expert = await response.json();
        document.getElementById('rating').innerText = expert.rating;
        document.getElementById('reviewCount').innerText = expert.reviewCount;
        document.getElementById('storeName').innerText = expert.storeName;
        document.getElementById('description').innerText = expert.description;
        document.getElementById('location').innerText = expert.location;
        document.getElementById('maxTravelDistance').innerText = expert.maxTravelDistance + 'km';

    } else {
        alert('고수 정보를 등록해 주세요');
    }
}

const cityDistrictMap = {
    "서울특별시": ["강남구", "강동구", "강북구", "강서구", "관악구", "광진구", "구로구", "금천구", "노원구", "도봉구", "동대문구", "동작구", "마포구", "서대문구", "서초구", "성동구", "성북구", "송파구", "양천구", "영등포구", "용산구", "은평구", "종로구", "중구", "중랑구"],
    "경기도": ["수원시 장안구", "수원시 권선구", "수원시 팔달구", "수원시 영통구", "성남시 수정구", "성남시 중원구", "성남시 분당구", "의정부시", "안양시 만안구", "안양시 동안구", "부천시", "광명시", "평택시", "동두천시", "안산시 상록구", "안산시 단원구", "고양시 덕양구", "고양시 일산동구",
        "고양시 일산서구", "과천시", "구리시", "남양주시", "오산시", "시흥시", "군포시", "의왕시", "하남시", "용인시 처인구", "용인시 기흥구", "용인시 수지구", "파주시", "이천시", "안성시", "김포시", "화성시", "광주시", "양주시", "포천시", "여주시", "연천군", "가평군",
        "양평군"],
    "인천광역시": ["계양구", "미추홀구", "남동구", "동구", "부평구", "서구", "연수구", "중구", "강화군", "옹진군"],
    "강원도": ["춘천시", "원주시", "강릉시", "동해시", "태백시", "속초시", "삼척시", "홍천군", "횡성군", "영월군", "평창군", "정선군", "철원군", "화천군", "양구군", "인제군", "고성군", "양양군"],
    "충청북도": ["청주시 상당구", "청주시 서원구", "청주시 흥덕구", "청주시 청원구", "충주시", "제천시", "보은군", "옥천군", "영동군", "증평군", "진천군", "괴산군", "음성군", "단양군"],
    "충청남도": ["천안시 동남구", "천안시 서북구", "공주시", "보령시", "아산시", "서산시", "논산시", "계룡시", "당진시", "금산군", "부여군", "서천군", "청양군", "홍성군", "예산군", "태안군"],
    "대전광역시": ["대덕구", "동구", "서구", "유성구", "중구"],
    "세종특별자치시": ["세종특별자치시"],
    "전라북도": ["전주시 완산구", "전주시 덕진구", "군산시", "익산시", "정읍시", "남원시", "김제시", "완주군", "진안군", "무주군", "장수군", "임실군", "순창군", "고창군", "부안군"],
    "전라남도": ["목포시", "여수시", "순천시", "나주시", "광양시", "담양군", "곡성군", "구례군", "고흥군", "보성군", "화순군", "장흥군", "강진군", "해남군", "영암군", "무안군", "함평군", "영광군", "장성군", "완도군", "진도군", "신안군"],
    "광주광역시": ["광산구", "남구", "동구", "북구", "서구"],
    "경상북도": ["포항시 남구", "포항시 북구", "경주시", "김천시", "안동시", "구미시", "영주시", "영천시", "상주시", "문경시", "경산시", "군위군", "의성군", "청송군", "영양군", "영덕군", "청도군", "고령군", "성주군", "칠곡군", "예천군", "봉화군", "울진군", "울릉군"],
    "경상남도": ["창원시 의창구", "창원시 성산구", "창원시 마산합포구", "창원시 마산회원구", "창원시 진해구", "진주시", "통영시", "사천시", "김해시", "밀양시", "거제시", "양산시", "의령군", "함안군", "창녕군", "고성군", "남해군", "하동군", "산청군", "함양군", "거창군", "합천군"],
    "부산광역시": ["강서구", "금정구", "남구", "동구", "동래구", "부산진구", "북구", "사상구", "사하구", "서구", "수영구", "연제구", "영도구", "중구", "해운대구", "기장군"],
    "대구광역시": ["남구", "달서구", "동구", "북구", "서구", "수성구", "중구", "달성군"],
    "울산광역시": ["남구", "동구", "북구", "중구", "울주군"],
    "제주특별자치도": ["서귀포시", "제주시"]
};

function fillCitySelect() {
    const citySelect = document.getElementById('citySelect');
    citySelect.innerHTML = '';

    Object.keys(cityDistrictMap).forEach(city => {
        const option = document.createElement('option');
        option.value = city;
        option.innerText = city;
        citySelect.appendChild(option);
    });
}

function fillDistrictSelect(city) {
    const districtSelect = document.getElementById('districtSelect');
    districtSelect.innerHTML = '';

    cityDistrictMap[city].forEach(district => {
        const option = document.createElement('option');
        option.value = district;
        option.innerText = district;
        districtSelect.appendChild(option);
    });
}


window.onload = () => {
    fetchProfileImage();
    fetchExpertProfile();
    loadSubItems();
    fillCitySelect();
    fillDistrictSelect(document.getElementById('citySelect').value);

    fetchProfileImage();
    fetchExpertProfile();

};

document.getElementById('citySelect').addEventListener('change', function () {
    fillDistrictSelect(this.value);
});


async function loadSubItems() {
    try {
        const token = localStorage.getItem('accessToken');

        const options = {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        };

        const response = await fetch("/api/v1/experts/sub-items", options);
        const data = await response.json();
        displaySubItems(data);
    } catch (error) {
        console.error('제공 서비스 로딩 중 오류 발생:', error);
    }
}

// 데이터를 받아서 처리하는 함수
function displaySubItems(data) {
    const container = document.getElementById('sub-items');
    container.innerHTML = "";

    // subItems가 존재하는지 확인
    if (data.subItemsResponse && data.subItemsResponse.length > 0) {
        data.subItemsResponse.forEach(subItem => {
            const button = document.createElement('button');
            button.className = "btn dashboard-btn btn-outline-primary";
            button.style.marginRight = '10px';
            button.innerText = subItem.name;

            const closeButton = document.createElement('span');
            closeButton.innerText = 'x';
            closeButton.onclick = () => removeSubItem(subItem.name);
            closeButton.style.marginLeft = '5px';
            closeButton.style.cursor = 'pointer';

            button.appendChild(closeButton);
            container.appendChild(button);
        });
    } else {
        // subItems가 없을 때 message 값을 출력
        const message = document.createElement('p');
        message.className = "alert alert-warning";
        message.innerText = data.message || "No sub-items available.";
        container.appendChild(message);
    }
}

/*
function displaySubItems(subItems) {
    const container = document.getElementById('sub-items');
    container.innerHTML = "";

    subItems.forEach(subItem => {
        const button = document.createElement('button');

        button.className = "btn dashboard-btn btn-outline-primary";

        button.style.marginRight = '10px';

        button.innerText = subItem.name;

        const closeButton = document.createElement('span');
        closeButton.innerText = 'x';
        closeButton.onclick = () => removeSubItem(subItem.name);

        closeButton.style.marginLeft = '5px';
        closeButton.style.cursor = 'pointer';

        button.appendChild(closeButton);
        container.appendChild(button);
    });
}
*/
async function removeSubItem(subItemName) {

    try {
        const token = localStorage.getItem('accessToken');
        const requestBody = {subItemName: subItemName};
        const response = await fetch("/api/v1/experts/sub-items", {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify(requestBody),
        });

        if (response.status === 204) {
            loadSubItems();
        } else {
            console.error('제공 서비스 삭제 중 오류 발생:', await response.text());
        }
    } catch (error) {
        console.error('제공 서비스 삭제 중 오류 발생:', error);
    }
}

async function loadServiceModalItems() {
    const token = localStorage.getItem('accessToken');
    const options = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        }
    };

    const response = await fetch('/api/v1/sub-items', options);
    const responseData = await response.json();

    if (response.ok) {
        const subItemSelect = document.getElementById("subItemSelect");
        subItemSelect.innerHTML = ""; // 기존 옵션 제거

        responseData.subItemsResponse.forEach(item => {
            const optionElement = document.createElement("option");
            optionElement.value = item.name;
            optionElement.innerText = item.name;
            subItemSelect.appendChild(optionElement);
        });
    } else {
        alert('서비스 항목들을 불러오는데 실패하였습니다.');
    }
}


const modal = document.getElementById("myModal");
const btn = document.getElementById("add-sub-items");
const span = document.querySelector(".close");

btn.onclick = function () {
    loadServiceModalItems();
    modal.style.display = "block";
}

span.onclick = function () {
    modal.style.display = "none";
}

window.onclick = function (event) {
    if (event.target === modal) {
        modal.style.display = "none";
    }
}

async function addSubItemToExpert() {
    const token = localStorage.getItem('accessToken');
    const subItemName = document.getElementById('subItemSelect').value;

    const requestBody = {
        subItemName: subItemName
    };

    const response = await fetch("/api/v1/experts/sub-items", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(requestBody),
    });

    if (response.ok) {
        alert('서비스가 성공적으로 등록되었습니다.');
        loadSubItems();
        modal.style.display = "none";
    } else {
        alert('서비스 등록에 실패하였습니다.');
    }
}

function openEditModal() {
    fetchExpertEditProfile();
    const modal = document.getElementById('editModal');
    modal.style.display = "block";
}

function closeEditModal() {
    const modal = document.getElementById('editModal');
    modal.style.display = "none";
}

async function fetchExpertEditProfile() {
    const token = localStorage.getItem('accessToken');

    const options = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        }
    };

    const response = await fetch('/api/v1/experts', options);

    if (response.ok) {
        const expert = await response.json();                
    	document.getElementById('storeNameInput').value = expert.storeName;
		// 도시와 구/군을 나누어 각각의 select box에 값 설정 
		const locationParts = expert.location.split(' ');
    	document.getElementById('citySelect').value = locationParts[0];
    	document.getElementById('districtSelect').value = locationParts[1];
                
        document.getElementById('maxTravelDistanceInput').value = expert.maxTravelDistance;
        document.getElementById('descriptionInput').value = expert.description;
    } else {
        alert('고수 프로필 정보를 불러오는데 실패하였습니다.');
    }
}

async function submitEdit() {
    const token = localStorage.getItem('accessToken');
    const city = document.getElementById('citySelect').value;
    const district = document.getElementById('districtSelect').value;
    const location = `${city} ${district}`;

    const requestBody = {
        storeName: document.getElementById('storeNameInput').value,
        location: location,
        maxTravelDistance: parseInt(document.getElementById('maxTravelDistanceInput').value),
        description: document.getElementById('descriptionInput').value
    };

    const options = {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(requestBody)
    };

    const response = await fetch('/api/v1/experts', options);

    if (response.ok) {
        alert('회원정보 수정이 완료되었습니다.');
        closeEditModal();
    } else {
        alert('회원정보 수정에 실패하였습니다.');
    }
}

function uploadProfileImage() {
    const token = localStorage.getItem('accessToken');
    let inputFile = document.getElementById('__BVID__773');
    let formData = new FormData();
    formData.append('file', inputFile.files[0]);

    fetch('/api/v1/members/profile/images', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        body: formData
    }).then(response => response.json()).then(data => {
        alert('프로필 이미지가 성공적으로 업로드되었습니다.');
    }).catch(error => {
        alert('프로필 이미지 업로드 중 오류가 발생했습니다.');
    });
}


/*
function setDefaultProfileImage() {
    const token = localStorage.getItem('accessToken');
    fetch('/api/v1/members/profile/images', {
        method: 'DELETE',
        headers: {
            'Authorization': 'Bearer ' + token
        }
    }).then(response => {
        if (response.ok) {
            alert('프로필 이미지가 기본 이미지로 변경되었습니다.');
        } else {
            alert('프로필 이미지 변경 중 오류가 발생했습니다.');
        }
    });
}
*/



async function setDefaultProfileImage() {
    const token = 'bearer ' + localStorage.getItem('accessToken');
    const imageUrl = '/img/item_image.jpg';
    if (document.querySelector('img').src === imageUrl) {
        return;
    }
	
	const response = await fetch(imageUrl);
    const blob = await response.blob();

    // Blob을 사용하여 File 객체 생성
    const imageFile = new File([blob], "item_image.jpg", { type: "image/jpg" });
	
     var formData = new FormData();
    formData.append('file', imageFile);

    $.ajax({
        type: 'POST',
        url: '/api/v1/members/profile/images',
        processData: false,
        contentType: false,
        data: formData,
        headers: {
            "Authorization": token
        },
        success: function (json) {
            alert("등록되었습니다.");
            window.location.reload();

        },
        error: function (xhr, status, error) {
            alert("이미지 등록에 실패했습니다." + error);
        }
    });
}

document.addEventListener("DOMContentLoaded", function () {
    loadImages();
});

function loadImages() {
    const token = localStorage.getItem('accessToken');

    fetch('/api/v1/experts/images', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        }
    })
        .then(response => response.json())
        .then(data => {
            renderImages(data.filenames);
        })
        .catch(error => {
            alert('이미지 로딩 중 오류가 발생했습니다.');
        });
}

function renderImages(filenames) {
    const container = document.getElementById('imagesContainer');
    container.innerHTML = '';

    filenames.forEach(filename => {
        const imageDiv = document.createElement('div');
        imageDiv.className = "image-item";
        imageDiv.style.position = 'relative';
        imageDiv.style.marginRight = '10px';

        const img = document.createElement('img');
        img.src = filename;
        img.alt = "Image";
        img.style.width = '100px';
        img.style.height = '100px';
        img.style.objectFit = 'cover';
        img.style.borderRadius = '5px';

        const deleteButton = document.createElement('span');
        deleteButton.innerHTML = "x";
        deleteButton.style.position = 'absolute';
        deleteButton.style.top = '0';
        deleteButton.style.right = '0';
        deleteButton.style.cursor = 'pointer';
        deleteButton.onclick = function () {
            deleteImage(filename);
        };

        imageDiv.appendChild(img);
        imageDiv.appendChild(deleteButton);
        container.appendChild(imageDiv);
    });
}

function deleteImage(filename) {
    const token = localStorage.getItem('accessToken');
    
    const pureFilename = filename.split('/').pop();

    fetch(`/api/v1/experts/images/${pureFilename}`, {
        method: 'DELETE',
        headers: {
            'Authorization': 'Bearer ' + token
        }
    })
        .then(response => {
            if (response.ok) {
                loadImages();
            } else {
                alert('이미지 삭제 중 오류가 발생했습니다.');
            }
        })
        .catch(error => {
            alert('이미지 삭제 중 오류가 발생했습니다.');
        });
}

function openImageUploadModal() {
    $('#imageUploadModal').modal('show');
}

function uploadExpertImage() {
    const token = localStorage.getItem('accessToken');
    let inputFile = document.getElementById('__BVID__774');
    let formData = new FormData();
    formData.append('file', inputFile.files[0]);

    fetch('/api/v1/experts/images', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        body: formData
    }).then(response => response.json()).then(data => {
        alert('이미지가 성공적으로 업로드되었습니다.');
        $('#profileImageModal').modal('hide');
    }).catch(error => {
        alert('이미지 업로드 중 오류가 발생했습니다.');
    });
}

function redirectToAutoQuote() {
    window.location.href = "/gosu-catcher/auto-quote";
}

function redirectToRequestedList() {
    window.location.href = "/gosu-catcher/expert-requested";
}
