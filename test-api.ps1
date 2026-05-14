# API Test Script for Ticket Booking System
# Tests all main endpoints

$BASE_URL = "http://localhost:8081"
$TOKEN = $null
$ADMIN_TOKEN = $null

function Print-Header {
    param($text)
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host $text -ForegroundColor Cyan
    Write-Host "========================================`n" -ForegroundColor Cyan
}

function Test-Endpoint {
    param(
        $method,
        $endpoint,
        $body = $null,
        $headers = @{},
        $description = ""
    )
    
    Write-Host "🧪 TEST: $description" -ForegroundColor Yellow
    Write-Host "   $method $endpoint" -ForegroundColor Gray
    
    try {
        $uri = "$BASE_URL$endpoint"
        $params = @{
            Uri = $uri
            Method = $method
            ContentType = "application/json"
            Headers = $headers
        }
        
        if ($body) {
            $params['Body'] = $body | ConvertTo-Json
        }
        
        $response = Invoke-RestMethod @params
        Write-Host "✅ SUCCESS" -ForegroundColor Green
        return $response
    }
    catch {
        Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# ==================== TEST 1: AUTH ENDPOINTS ====================
Print-Header "TEST 1: AUTHENTICATION"

# Register user
$registerBody = @{
    email = "user@example.com"
    password = "password123"
    fullName = "Test User"
}
$registerResp = Test-Endpoint "POST" "/api/auth/register" $registerBody -description "Register new user"

# Login
$loginBody = @{
    email = "user@example.com"
    password = "password123"
}
$loginResp = Test-Endpoint "POST" "/api/auth/login" $loginBody -description "User login"
if ($loginResp -and $loginResp.accessToken) {
    $TOKEN = $loginResp.accessToken
    Write-Host "   Token: $($TOKEN.Substring(0, 20))..." -ForegroundColor Gray
}

# Admin Login
$adminLoginBody = @{
    email = "admin@example.com"
    password = "admin123"
}
$adminLoginResp = Test-Endpoint "POST" "/api/auth/login" $adminLoginBody -description "Admin login"
if ($adminLoginResp -and $adminLoginResp.accessToken) {
    $ADMIN_TOKEN = $adminLoginResp.accessToken
    Write-Host "   Admin Token: $($ADMIN_TOKEN.Substring(0, 20))..." -ForegroundColor Gray
}

# Get current user
$headers = @{ "Authorization" = "Bearer $TOKEN" }
Test-Endpoint "GET" "/api/auth/me" -headers $headers -description "Get current user info" | Out-Null

# ==================== TEST 2: GENRE ENDPOINTS ====================
Print-Header "TEST 2: GENRES"

# Get all genres
$genresResp = Test-Endpoint "GET" "/api/genres" -description "Get all genres"

# Create genre (admin only)
$headers = @{ "Authorization" = "Bearer $ADMIN_TOKEN" }
$genreBody = @{
    name = "Sci-Fi"
    description = "Science fiction movies"
}
$createGenreResp = Test-Endpoint "POST" "/api/genres" $genreBody -headers $headers -description "Create new genre"
if ($createGenreResp -and $createGenreResp.id) {
    $GENRE_ID = $createGenreResp.id
    Write-Host "   Genre ID: $GENRE_ID" -ForegroundColor Gray
}

# ==================== TEST 3: MOVIE ENDPOINTS ====================
Print-Header "TEST 3: MOVIES"

# Get all movies
$moviesResp = Test-Endpoint "GET" "/api/movies" -description "Get all movies"

# Create movie
$movieBody = @{
    title = "Dune"
    description = "Epic sci-fi movie"
    duration = 166
    genreId = $GENRE_ID
}
$headers = @{ "Authorization" = "Bearer $ADMIN_TOKEN" }
$createMovieResp = Test-Endpoint "POST" "/api/movies" $movieBody -headers $headers -description "Create new movie"
if ($createMovieResp -and $createMovieResp.id) {
    $MOVIE_ID = $createMovieResp.id
    Write-Host "   Movie ID: $MOVIE_ID" -ForegroundColor Gray
}

# Get movie by ID
Test-Endpoint "GET" "/api/movies/$MOVIE_ID" -description "Get movie details" | Out-Null

# ==================== TEST 4: ROOM ENDPOINTS ====================
Print-Header "TEST 4: ROOMS"

# Get all rooms
$roomsResp = Test-Endpoint "GET" "/api/rooms" -description "Get all rooms"

# Create room
$roomBody = @{
    name = "Room A"
    totalSeats = 100
}
$headers = @{ "Authorization" = "Bearer $ADMIN_TOKEN" }
$createRoomResp = Test-Endpoint "POST" "/api/rooms" $roomBody -headers $headers -description "Create new room"
if ($createRoomResp -and $createRoomResp.id) {
    $ROOM_ID = $createRoomResp.id
    Write-Host "   Room ID: $ROOM_ID" -ForegroundColor Gray
}

# ==================== TEST 5: SEAT ENDPOINTS ====================
Print-Header "TEST 5: SEATS"

# Get seats by room
Test-Endpoint "GET" "/api/rooms/$ROOM_ID/seats" -description "Get seats in room" | Out-Null

# Get seat status
$seatStatusResp = Test-Endpoint "GET" "/api/rooms/$ROOM_ID/seat-status" -description "Get room seat status" | Out-Null

# ==================== TEST 6: SHOWTIME ENDPOINTS ====================
Print-Header "TEST 6: SHOWTIMES"

# Get all showtimes
$showtimesResp = Test-Endpoint "GET" "/api/showtimes" -description "Get all showtimes"

# Create showtime
$showtimeBody = @{
    movieId = $MOVIE_ID
    roomId = $ROOM_ID
    startTime = "2026-05-20T14:00:00"
    price = 150000
}
$headers = @{ "Authorization" = "Bearer $ADMIN_TOKEN" }
$createShowtimeResp = Test-Endpoint "POST" "/api/showtimes" $showtimeBody -headers $headers -description "Create new showtime"
if ($createShowtimeResp -and $createShowtimeResp.id) {
    $SHOWTIME_ID = $createShowtimeResp.id
    Write-Host "   Showtime ID: $SHOWTIME_ID" -ForegroundColor Gray
}

# Get showtime by ID
Test-Endpoint "GET" "/api/showtimes/$SHOWTIME_ID" -description "Get showtime details" | Out-Null

# ==================== TEST 7: BOOKING ENDPOINTS ====================
Print-Header "TEST 7: BOOKINGS"

# Get available seats
$headers = @{}
$availableSeatsResp = Test-Endpoint "GET" "/api/showtimes/$SHOWTIME_ID/available-seats" -headers $headers -description "Get available seats for showtime"

# Get first available seat
if ($availableSeatsResp -and $availableSeatsResp.Count -gt 0) {
    $SEAT_ID = $availableSeatsResp[0]
    Write-Host "   Selected Seat ID: $SEAT_ID" -ForegroundColor Gray
}

# Hold seat
$holdBody = @{
    showtimeId = $SHOWTIME_ID
    seatIds = @($SEAT_ID)
}
$headers = @{ "Authorization" = "Bearer $TOKEN" }
$holdResp = Test-Endpoint "POST" "/api/bookings/hold" $holdBody -headers $headers -description "Hold seats"
if ($holdResp -and $holdResp.id) {
    $BOOKING_ID = $holdResp.id
    Write-Host "   Booking ID: $BOOKING_ID" -ForegroundColor Gray
}

# Checkout (create booking)
$checkoutBody = @{
    bookingId = $BOOKING_ID
}
$checkoutResp = Test-Endpoint "POST" "/api/bookings/checkout" $checkoutBody -headers $headers -description "Checkout booking"

# Get user bookings
Test-Endpoint "GET" "/api/bookings/my-bookings" -headers $headers -description "Get user's bookings" | Out-Null

# ==================== TEST 8: SWAGGER/API DOCS ====================
Print-Header "TEST 8: API DOCUMENTATION"

# Check Swagger UI
Test-Endpoint "GET" "/swagger-ui.html" -description "Swagger UI (should be accessible)" | Out-Null

# Check OpenAPI spec
Test-Endpoint "GET" "/v3/api-docs" -description "OpenAPI specification" | Out-Null

Print-Header "✅ ALL TESTS COMPLETED"
Write-Host "Summary of tested endpoints:" -ForegroundColor Cyan
Write-Host "✓ Authentication (register, login, me)" -ForegroundColor Green
Write-Host "✓ Genres (list, create)" -ForegroundColor Green
Write-Host "✓ Movies (list, create, get by ID)" -ForegroundColor Green
Write-Host "✓ Rooms (list, create)" -ForegroundColor Green
Write-Host "✓ Seats (get by room, seat status)" -ForegroundColor Green
Write-Host "✓ Showtimes (list, create, get by ID, available seats)" -ForegroundColor Green
Write-Host "✓ Bookings (hold, checkout, get user bookings)" -ForegroundColor Green
Write-Host "✓ Swagger/API Docs" -ForegroundColor Green
